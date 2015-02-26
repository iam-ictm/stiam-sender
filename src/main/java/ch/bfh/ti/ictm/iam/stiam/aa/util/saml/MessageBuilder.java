/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util.saml;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.opensaml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * This is the base class for all classes building SAML-messages. It provides
 * some general helper methods used by all subclasses.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public abstract class MessageBuilder {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(MessageBuilder.class);

//////////////////////////////////////// Methods    
    /**
     * Helper method for building XMLObjects according to a given
     * QName-instance. This method retrieves the appropriate builder for a given
     * QName and then uses it to create an XMLObject which is then returned.
     *
     * @param qName QName of the XMLObject to build
     * @return The generated XMLObject
     */
    protected XMLObject buildXMLObject(QName qName) {
        @SuppressWarnings("unchecked") // as we're obtaining Builders based on given QName...
        final XMLObjectBuilder<XMLObject> builder = Configuration.getBuilderFactory().getBuilder(qName);
        if (builder == null) {
            logger.error("No builder obtained for QName: {}", qName.toString());
            return null;
        }

        return builder.buildObject(qName.getNamespaceURI(), qName.getLocalPart(),
                qName.getPrefix());
    }

    /**
     * Marshalls a given XMLObject to String
     *
     * @param obj The XMLObject to marshall
     * @return The marshalled String representation of obj
     * @throws MarshallingException
     * @throws XMLParserException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    protected String marshallToString(XMLObject obj) throws MarshallingException, XMLParserException,
            TransformerConfigurationException, TransformerException {
        logger.debug("Starting marshalling of extended attribute query...");

        final Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(obj);
        final Document doc = new BasicParserPool().newDocument();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

        marshaller.marshall(obj, doc);
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        final String xmlString = new String(outputStream.toByteArray());

        logger.debug("Done! Marshalled XML data: {} ", xmlString);
        return xmlString;
    }

    /**
     * @return Base64-encoded version of the message produced by build()
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
    public String buildBase64() throws ConfigurationException,
            NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
            UnrecoverableEntryException, org.opensaml.xml.security.SecurityException, MarshallingException,
            SignatureException, XMLParserException, TransformerException {
        return Base64.encodeBytes(build().getBytes(StiamConfiguration.getInstance().getAttributeQueryEncoding()));
    }

//////////////////////////////////////// Abstract Methods
    /**
     * This method is overridden by concrete implementations of this class. It
     * builds a SAML-message depending on the type of the object implementing
     * it.
     *
     * @return A String containing the SAML-message in unencoded form.
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
    public abstract String build() throws ConfigurationException,
            NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
            UnrecoverableEntryException, org.opensaml.xml.security.SecurityException, MarshallingException,
            SignatureException, XMLParserException, TransformerException;
}
