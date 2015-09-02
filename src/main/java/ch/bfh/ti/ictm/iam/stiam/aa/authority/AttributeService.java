/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.authority;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.Directory;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryFactory;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap.NameIDNotFoundException;
import ch.bfh.ti.ictm.iam.stiam.aa.eligibility.EligibilityChecker;
import ch.bfh.ti.ictm.iam.stiam.aa.eligibility.EligibilityCheckerFactory;
import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import ch.bfh.ti.ictm.iam.stiam.aa.util.saml.Attribute;
import ch.bfh.ti.ictm.iam.stiam.aa.util.saml.AttributeResponseBuilder;
import ch.bfh.ti.ictm.iam.stiam.aa.util.saml.ResponseBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.BaseSAMLMessageDecoder;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.decoding.HTTPSOAP11Decoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AttributeService-servlet of the STIAM attribute authority. Receives extended
 * SAML attribute queries and handles them.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@WebServlet(urlPatterns = {"/"})
@SuppressWarnings("serial")
public class AttributeService extends HttpServlet {

//////////////////////////////////////// Fields
    private static final Logger logger = LoggerFactory.getLogger(AttributeService.class);
    private static final StiamConfiguration config = StiamConfiguration.getInstance();
    private static EligibilityChecker eligibilityChecker;
    private static Directory directory;

//////////////////////////////////////// Methods
    /**
     * Inherited from HttpServlet, some basic initialization is performed here.
     *
     * @throws ServletException If initialisation fails for some reason.
     */
    @Override
    public void init() throws ServletException {
        try {
            DefaultBootstrap.bootstrap();   // initialise OpenSAML
            directory = DirectoryFactory.getInstance().createDirectory();
            eligibilityChecker = EligibilityCheckerFactory.getInstance().createEligibilityChecker();
        } catch (ConfigurationException ex) {
            logger.error("Error initializing attribute service: {}", ex.getMessage());
            throw new ServletException(ex);
        }
        logger.info("Sucessfully initialized Attribute Service (AS)!");
    }

    /**
     * Method inherited from HttpServlet. As all SAML-requests are received with
     * the POST-method, we show a short information page on GET-requests to
     * inform the user/operator that the AA is up and running.
     *
     * @param req The request-instance obtained from the container
     * @param res The response-instance obtained from the container
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setStatus(200);
        res.setContentType("text/html");
        try {
            final PrintWriter pw = res.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<head>");
            pw.println("<meta charset=\"utf-8\"/>");
            pw.println("<title>STIAM Attribute Authority</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<h1>Welcome to the STIAM Attribute Authority!</h1>");
            pw.println("The authority is <b>up and running</b>, accepting attribute requests via HTTP POST.");
            pw.println("</body>\n</html>");
        } catch (IOException ex) {
            logger.error("Cannot send infopage, unable to write to response: {}", ex.getMessage());
        }
    }

    /**
     * Method inherited from HttpServlet. Handles POST-requests, tries to
     * extract extended SAML attribute queries from the request and return
     * appropriate responses.
     *
     * @param req The request-instance obtained from the container
     * @param res The response-instance obtained from the container
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("Request received!");
        final DateTime receptionTime = DateTime.now();

        //////////////////// Decode raw request
        logger.debug("Trying to decode raw request...");
        final MessageContext messageContext = new BasicSAMLMessageContext();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(req));
        final BaseSAMLMessageDecoder messageDecoder;
        if (config.getBinding() == StiamConfiguration.Binding.HTTP_POST) {
            logger.debug("Using HTTPPostDecoder for decoding...");
            messageDecoder = new HTTPPostDecoder();
        } else {
            logger.debug("Using HTTPSOAP11Decoder for decoding...");
            messageDecoder = new HTTPSOAP11Decoder();
        }

        try {
            messageDecoder.decode(messageContext);
        } catch (MessageDecodingException | SecurityException | IllegalArgumentException ex) {
            sendSAMLError(res, 400, "Decoding failed: " + ex.getMessage(), "", "",
                    new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                        ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
            return;
        }
        logger.debug("Decoding succeeded!");

        //////////////////// Read out attribute-query
        logger.debug("Trying to read AttributeQuery...");
        AttributeQuery attributeQuery = null;
        try {
            if (config.getBinding() == StiamConfiguration.Binding.HTTP_POST) {
                attributeQuery = (AttributeQuery) messageContext.getInboundMessage();
            } else {
                final Envelope soapEnvelope = (Envelope) messageContext.getInboundMessage();
                final List<XMLObject> bodyElements = soapEnvelope.getBody().getUnknownXMLObjects();

                for (XMLObject element : bodyElements) {
                    if (element instanceof AttributeQuery) {
                        attributeQuery = (AttributeQuery) element;
                    }
                }
            }
        } catch (ClassCastException ex) {
            sendSAMLError(res, 400, "AttributeQuery could not be read!", "", "",
                    new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                        ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
            return;
        }

        if (attributeQuery == null) {
            sendSAMLError(res, 400, "No AttributeQuery found!", "", "",
                    new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                        ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
            return;
        }

        logger.debug("Sucessfully read AttributeQuery!");

        //////////////////// Try to read out QueryID, Issuer and NameID of the query
        logger.debug("Reading QueryID, Issuer and NameID of the query...");
        final String queryID;
        final String queryIssuer;
        final String nameID;
        try {
            queryID = attributeQuery.getID();
            queryIssuer = attributeQuery.getIssuer().getValue();
            nameID = attributeQuery.getSubject().getNameID().getValue();
        } catch (Exception ex) {
            sendSAMLError(res, 400, "Unable to read essential attributes of the query!", "", "",
                    new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                        ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
            return;
        }
        logger.debug("Query with ID '{}' received from issuer '{}' for subject '{}'.", queryID, queryIssuer, nameID);

        //////////////////// Verify signature of the attribute query
        if (config.verifyQuerySignature()) {
            logger.debug("Trying to verify signature of the attribute query...");
            if (!verifySignature(attributeQuery.getSignature(), attributeQuery.getIssuer().getValue().toString())) {
                sendSAMLError(res, 400, "Signature validation failed!", queryIssuer, queryID,
                        new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                            ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
                return;
            }
            logger.debug("Signature verified successfully!");
        }

        //////////////////// Check if subject is eligible...
        logger.debug("Checking subject eligibility...");
        if (!eligibilityChecker.isEligible(nameID)) {
            sendSAMLError(res, 400, "Subject not eligible!", queryIssuer, queryID,
                    new String[]{ResponseBuilder.STATUS_CODE_RESPONDER,
                        ResponseBuilder.STATUS_CODE_UNKNOWN_PRINCIPAL});
            return;
        }
        logger.debug("Subject is eligible, continueing");

        //////////////////// Verify if we have extensions and if they contain an authentication statement
        if (config.verifyAuthnStatement()) {
            logger.debug("Trying to verify embedded Authn-Assertion...");

            logger.debug("Reading out assertion...");
            final AuthnStatement authnStatement;
            final Assertion assertion;
            final String assertionNameID;
            try {
                // Note: this code only works for the very limited assumption that we only have one
                // extension in the query (which is to be assumed to be an Assertion) and that this
                // in turn contains exactly one AuthnStatement. For other than PoC-code, this must be
                // implemented properly...
                assertion = (Assertion) attributeQuery.getExtensions().getUnknownXMLObjects().get(0);
                assertionNameID = assertion.getSubject().getNameID().getValue();
                authnStatement = (AuthnStatement) assertion.getStatements().get(0);
                if (authnStatement == null) {
                    sendSAMLError(res, 400, "Unable to read embedded authentication statement!", queryIssuer, queryID,
                            new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                                ResponseBuilder.STATUS_CODE_NO_AUTHN_CONTEXT});
                    return;
                }
            } catch (Exception ex) {
                sendSAMLError(res, 400, "Unable to read embedded authentication statement!", queryIssuer, queryID,
                        new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                            ResponseBuilder.STATUS_CODE_NO_AUTHN_CONTEXT});
                return;
            }
            logger.debug("Successfully read assertion!");

            //////////////////// Compare NameID of authentication assertion to attribute query
            logger.debug("Ensuring that NameIDs match...");
            if (!assertionNameID.equals(nameID)) {
                sendSAMLError(res, 400, "NameID of assertion does not match NameID of attribute query!", queryIssuer, queryID,
                        new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                            ResponseBuilder.STATUS_CODE_NO_AUTHN_CONTEXT});
                return;
            }
            logger.debug("NameIDs are equal!");

            //////////////////// Verify signature of the assertion
            if (config.verifyAuthnSignature()) {
                logger.debug("Trying to validate signature of authentication statement...");
                if (!verifySignature(assertion.getSignature(), assertion.getIssuer().getValue().toString())) {
                    sendSAMLError(res, 400, "Signature validation failed!", queryIssuer, queryID,
                            new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                                ResponseBuilder.STATUS_CODE_NO_AUTHN_CONTEXT});
                    return;
                }
                logger.debug("Signature verified successfully!");
            }

            //////////////////// Verify NotBefore / NotOnOrAfter in the Conditions of the authentication statement
            if (config.verifyAuthnTimespan()) {
                logger.debug("Trying to validate authentication-timespan...");
                if (receptionTime.isBefore(assertion.getConditions().getNotBefore())
                        || receptionTime.isEqual(assertion.getConditions().getNotOnOrAfter())
                        || receptionTime.isAfter(assertion.getConditions().getNotOnOrAfter())) {
                    sendSAMLError(res, 400, "Received statement is not in authentication-timespan!", queryIssuer, queryID,
                            new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                                ResponseBuilder.STATUS_CODE_NO_AUTHN_CONTEXT});
                    return;
                }
                logger.debug("Authentication-timespan verifed!");
            }

            logger.debug("Authn-Assertion found and validated!");
        }

        //////////////////// Read out attributes
        logger.debug("Reading Attributes...");
        final HashMap<String, Attribute> attributes = new HashMap<>(10);
        for (org.opensaml.saml2.core.Attribute attr : attributeQuery.getAttributes()) {
            attributes.put(attr.getName(), new Attribute(attr));
        }
        if (attributes.isEmpty()) {
            sendSAMLError(res, 400, "No attributes found in query!", queryIssuer, queryID,
                    new String[]{ResponseBuilder.STATUS_CODE_REQUESTER,
                        ResponseBuilder.STATUS_CODE_REQUEST_DENIED});
            return;
        }
        logger.debug("Found {} attributes in query", attributes.size());

        for (Attribute attr : attributes.values()) {
            logger.debug("Attribute: {}", attr);
        }

        //////////////////// Query attributes in directory
        logger.debug("Retrieving attributes from directory...");
        try {
            final String[] attributeNames = new String[attributes.size()];
            int i = 0;
            for (String name : attributes.keySet()) {
                attributeNames[i] = name;
                i++;
            }
            final Map<String, String> fetchedAttributes = directory.fetchAttributes(nameID, attributeNames);

            for (String name : attributeNames) {
                attributes.get(name).setValue(fetchedAttributes.get(name));
                logger.debug("Got value: {}", attributes.get(name));
            }
        } catch (NameIDNotFoundException ex) {
            sendSAMLError(res, 400, "Subject not found!", queryIssuer, queryID,
                    new String[]{ResponseBuilder.STATUS_CODE_RESPONDER,
                        ResponseBuilder.STATUS_CODE_UNKNOWN_PRINCIPAL});
            return;
        } catch (DirectoryException ex) {
            sendError(res, 500, "Error while fetching Attributes in directory: " + ex.getMessage());
            return;
        }
        logger.debug("Attributes retrieved!");

        //////////////////// Return attribute assertion
        logger.debug("Sending response...");
        try {
            logger.debug("Building attribute response...");
            final AttributeResponseBuilder builder = new AttributeResponseBuilder(queryIssuer, queryID, nameID, attributes.values());
            if (config.getBinding() == StiamConfiguration.Binding.HTTP_POST) {
                res.setStatus(200);
                res.setContentType("text/html");

                final PrintWriter pw = res.getWriter();
                pw.println("<!DOCTYPE html>");
                pw.println("<html><head>\n<meta charset=\"utf-8\"/>\n<title>SAMLResponse</title>\n</head>");
                pw.println("<body onload=\"function () { document.forms[0].submit(); }\">");
                pw.println("<form method=\"post\" action=\"" + queryIssuer + "\">");
                pw.println("<input type=\"hidden\" name=\"SAMLResponse\" value=\""
                        + URLEncoder.encode(builder.buildBase64(), "UTF-8") + "\"/>");
                pw.println("</form>");
                pw.println("</body>\n</html>");
            } else {
                res.setStatus(200);
                res.setContentType("text/xml;charset=UTF-8");

                final PrintWriter pw = res.getWriter();
                pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                pw.print("<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap11:Body>");
                pw.print(builder.build().substring(38));    // FIXME ugly substring-hack
                pw.print("</soap11:Body></soap11:Envelope>");
            }
        } catch (ConfigurationException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                UnrecoverableEntryException | SecurityException | MarshallingException | SignatureException |
                XMLParserException | TransformerException ex) {
            sendError(res, 500, "Error while building attribute response: " + ex.getMessage());
            return;
        }

        logger.info("Request handled!");
    }

    /**
     * Helper method to verify a SAML signature
     *
     * @param signature the signature to verify
     * @return true if verification was successful, false if not
     */
    private boolean verifySignature(Signature signature, String alias) {
    	try {
            final SignatureValidator signatureValidator = new SignatureValidator(config.getVerificationCredential(alias));
            signatureValidator.validate(signature);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException | IOException ex) {
            logger.error("Error while obtaining verification credential: {}", ex.getMessage());
            return false;
        } catch (ValidationException ex) {
            logger.error("Error while validating signature: {}", ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Helper method for sending a SAML status-messages with the given
     * statuscodes.
     *
     * @param res The HttpServletResponse used for sending the message
     * @param httpStatusCode The HTTP-statuscode to set on the returned message
     * @param message A message used for logging the error on the AA
     * @param destination SAML-destination of the message
     * @param queryID ID of the SAML-query causing the message
     * @param statusCodes An array of statuscodes to include in the message
     */
    private void sendSAMLError(HttpServletResponse res, int httpStatusCode, String message,
            String destination, String queryID, String[] statusCodes) {
        logger.error(message);
        logger.debug("Sending error as SAML status response with the following status code(s): {}", (Object[]) statusCodes);

        res.setStatus(httpStatusCode);
        res.setContentType("text/plain");
        try {
            final ResponseBuilder builder = new ResponseBuilder(destination, queryID, statusCodes);

            final PrintWriter pw = res.getWriter();
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.print("<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap11:Body>");
            pw.print(builder.build().substring(38));    // FIXME ugly substring-hack
            pw.print("</soap11:Body></soap11:Envelope>");
            
        } catch (ConfigurationException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                UnrecoverableEntryException | SecurityException | MarshallingException | SignatureException |
                XMLParserException | TransformerException | IOException ex) {
            sendError(res, 500, "Error while sending error, cause: {}" + ex.getMessage());
        }
    }

    /**
     * Helper method for sending a textual error message.
     *
     * @param res The HttpServletResponse used for sending the message
     * @param httpStatusCode The HTTP-statuscode to set on the returned message
     * @param message The message which is logged and sent to the requester
     */
    private void sendError(HttpServletResponse res, int httpStatusCode, String message) {
        logger.error(message);

        res.setStatus(httpStatusCode);
        res.setContentType("text/plain");
        try {
            res.getWriter().println(message);
        } catch (IOException ex1) {
            logger.error("Cannot send error, unable to write to response: {}", ex1.getMessage());
        }
    }
}
