/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util.saml;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import javax.xml.transform.TransformerException;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class builds a SAML status response
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class ResponseBuilder extends MessageBuilder {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(ResponseBuilder.class);
    private static final StiamConfiguration config = StiamConfiguration.getInstance();

    protected final String destination;
    protected final String inResponseTo;
    protected final String[] statusCodes;
    protected final String messageId;
    protected final DateTime issueInstant;

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_REQUESTER = "urn:oasis:names:tc:SAML:2.0:status:Requester";

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_RESPONDER = "urn:oasis:names:tc:SAML:2.0:status:Responder";

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_REQUEST_DENIED = "urn:oasis:names:tc:SAML:2.0:status:RequestDenied";

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_NO_AUTHN_CONTEXT = "urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext";

    /**
     * A status-code defined by SAML 2.0
     */
    public static final String STATUS_CODE_UNKNOWN_PRINCIPAL = "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal";

//////////////////////////////////////// Constructors
    /**
     * Default constructor, allowing subclasses to have constructors with
     * different parameters
     */
    private ResponseBuilder() {
        this.destination = "";
        this.inResponseTo = "";
        this.statusCodes = new String[]{STATUS_CODE_SUCCESS};
        this.messageId = "";
        issueInstant = DateTime.now();
    }

    /**
     * Initialises a builder for status responses with the given status codes.
     *
     * @param destination The recipient of the response
     * @param inResponseTo ID from the requesting extended responseAttribute
     * query
     * @param statusCodes An array of statuscodes to include in the response
     * @throws org.opensaml.xml.ConfigurationException
     * @throws java.security.NoSuchAlgorithmException
     */
    public ResponseBuilder(String destination, String inResponseTo, String[] statusCodes) throws ConfigurationException, NoSuchAlgorithmException {
        this.destination = destination;
        this.inResponseTo = inResponseTo;
        this.statusCodes = statusCodes;

        DefaultBootstrap.bootstrap();
        messageId = new SecureRandomIdentifierGenerator().generateIdentifier();
        issueInstant = DateTime.now();
    }

//////////////////////////////////////// Methods
    /**
     * Overrides the method in MessageBuilder in order to generate a SAML
     * status message with multiple status codes
     *
     * @return
     * @throws ConfigurationException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     * @throws SecurityException
     * @throws MarshallingException
     * @throws SignatureException
     * @throws XMLParserException
     * @throws TransformerException
     */
    @Override
    public String build() throws ConfigurationException, NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableEntryException, SecurityException, MarshallingException, SignatureException, XMLParserException, TransformerException {
        logger.debug("Starting generation of status response...");

        final Response response = buildRawResponse();

        //////////////////// Sign the response
        final Credential signingCredential = StiamConfiguration.getInstance().getSignatureCredential();
        final Signature responseSignature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(responseSignature, signingCredential, null, null);
        response.setSignature(responseSignature);
        Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(responseSignature);

        logger.debug("Attribute response generated!");
        return marshallToString(response);
    }

    /**
     * Builds a raw, unsigned response for further usage by build() or according
     * methods of subclasses.
     *
     * @return The raw, unsigned response object
     * @throws ConfigurationException
     * @throws NoSuchAlgorithmException
     */
    protected Response buildRawResponse() throws ConfigurationException, NoSuchAlgorithmException {
        // Response - the outermost element
        final Response response = (Response) buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
        response.setVersion(SAMLVersion.VERSION_20);
        response.setID(messageId);
        response.setInResponseTo(inResponseTo);
        response.setIssueInstant(issueInstant);
        response.setDestination(destination);

        // Issuer of the response
        final Issuer responseIssuer = (Issuer) buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setValue(config.getSAMLIssuer());
        response.setIssuer(responseIssuer);

        // Status of the response
        final Status status = (Status) buildXMLObject(Status.DEFAULT_ELEMENT_NAME);
        response.setStatus(status);

        // --> Outermost StatusCode of the Status
        LinkedList<StatusCode> statusQueue = new LinkedList<>();
        for (String code : statusCodes) {
            StatusCode current = (StatusCode) buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
            current.setValue(code);
            statusQueue.addLast(current);
        }

        StatusCode current = statusQueue.pollFirst();
        status.setStatusCode(current);
        while (!statusQueue.isEmpty()) {
            current.setStatusCode(statusQueue.getFirst());
            current = statusQueue.pollFirst();
        }

        return response;
    }
}
