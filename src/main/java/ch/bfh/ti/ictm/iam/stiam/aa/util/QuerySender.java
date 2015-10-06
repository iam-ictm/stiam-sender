/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util;

import ch.bfh.ti.ictm.iam.stiam.aa.util.saml.ExtendedAttributeQueryBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.transform.TransformerException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a testclient which can be used to send extended SAML attribute
 * queries to a remote attribute authority.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
class QuerySender {
//////////////////////////////////////// Fields

    private static final String DEFAULT_BINDING = "soap";
    private static final String CONFIG_Binding = "QuerySender.Binding";
    private static final String CONFIG_AS_URL = "QuerySender.ASURL";
    private static final String CONFIG_ATTRIBUTE_LIST = "QuerySender.AttributeList";

    private static final Logger logger = LoggerFactory.getLogger(QuerySender.class);
    private static QuerySenderConfiguration config = new QuerySenderConfiguration();

//////////////////////////////////////// Methods
    /**
     * Main-method of this class, sends a simulated POST-query.
     *
     * @param args Arguments given to the main call
     */
    public static void main(String[] args) {
        try {
            config.load();
        } catch (IOException ex) {
            config = null;
        }

        String asUrl = null;
        if (args.length == 1) {
            asUrl = args[0];
        } else if (config != null && config.getProperty(CONFIG_AS_URL) != null && !config.getProperty(CONFIG_AS_URL).isEmpty()) {
            asUrl = config.getProperty(CONFIG_AS_URL);
        }

        if (asUrl == null) {
            logger.error(("URL of Attribute Service must be given in configuration or on commandline!"));
            System.exit(10);
        }

        try {
            logger.info("POSTing extended attribute query to {}...", asUrl);

            final HttpURLConnection connection = (HttpURLConnection) new URL(asUrl).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "*/*");

            final String bodyData;
            if (config.getProperty(CONFIG_Binding, DEFAULT_BINDING).equalsIgnoreCase(DEFAULT_BINDING)) {
                connection.setRequestProperty("Content-Type", "text/xml");
                bodyData = createSOAPBindingData();
            } else {
                bodyData = createPOSTBindingData();
            }

            try (PrintStream ps = new PrintStream(connection.getOutputStream())) {
                ps.print(bodyData);
            }
            connection.connect();

            logger.info("Query Sent! Response-Code: {}", connection.getResponseCode());

            String response = "";
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
            }
            logger.info("Response: {}", response);
        } catch (MalformedURLException ex) {
            logger.error("Malformed AA-AS URL: {}", ex.getMessage());
        } catch (IOException ex) {
            logger.error("IO-Error occured: {}", ex.getMessage());
        } catch (ConfigurationException ex) {
            logger.error("Configuration problem: {}", ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Error finding algorithm: {}", ex.toString());
        } catch (KeyStoreException ex) {
            logger.error("Error while intializing keystore: {}", ex.toString());
        } catch (CertificateException ex) {
            logger.error("Error with the certificate: {}", ex.toString());
        } catch (UnrecoverableEntryException ex) {
            logger.error("Unrecoverable entry: {}", ex.toString());
        } catch (org.opensaml.xml.security.SecurityException ex) {
            logger.error("Security problem: {}", ex.toString());
        } catch (MarshallingException ex) {
            logger.error("Issue while marshalling: {}", ex.toString());
        } catch (SignatureException ex) {
            logger.error("Issue while signing: {}", ex.toString());
        } catch (XMLParserException ex) {
            logger.error("Problem with the XML parser: {}", ex.toString());
        } catch (TransformerException ex) {
            logger.error("Problem with the XML transformer: {}", ex.toString());
        }
    }

    /**
     * Helper method for parsing the attribute-specifications in the
     * configuration file.
     *
     * @return An ArrayList of String-arrays as expected by the builder
     */
    private static ArrayList<String[]> buildAttributes() {
        final ArrayList<String[]> attributes = new ArrayList<>(10);
        if (config != null) {
            logger.info("Reading attribute list from configuration...");
            final String[] attributeProperties = config.getPropertyList(CONFIG_ATTRIBUTE_LIST);
            if (attributeProperties != null) {
                for (String attributeProperty : attributeProperties) {
                    logger.info("Attribute: {}", Arrays.toString(config.getPropertyList(attributeProperty)));
                    attributes.add(config.getPropertyList(attributeProperty));
                }
            }
        }
        return attributes;
    }

    private static String createPOSTBindingData() throws ConfigurationException, UnsupportedEncodingException,
            NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
            UnrecoverableEntryException, SecurityException, MarshallingException, SignatureException,
            XMLParserException, TransformerException {
        // Generation and encoding of the request. Note: UTF-8 is taken for URL-encoding independently
        // of the encoding specified in configuration which applies to the SAML-messages only.
        // Refer to http://docs.oracle.com/javase/7/docs/api/java/net/URLEncoder.html#encode(java.lang.String,%20java.lang.String)
        // for more information.
        final ExtendedAttributeQueryBuilder builder = new ExtendedAttributeQueryBuilder(buildAttributes());
        return "SAMLRequest=" + URLEncoder.encode(builder.buildBase64(), "UTF-8");
    }

    // FIXME ugly substring-hack
    private static String createSOAPBindingData() throws ConfigurationException, UnsupportedEncodingException,
            NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
            UnrecoverableEntryException, SecurityException, MarshallingException, SignatureException,
            XMLParserException, TransformerException {
        final ExtendedAttributeQueryBuilder builder = new ExtendedAttributeQueryBuilder(buildAttributes());
        return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + builder.build().substring(38) + "</S:Body></S:Envelope>";
    }

}
