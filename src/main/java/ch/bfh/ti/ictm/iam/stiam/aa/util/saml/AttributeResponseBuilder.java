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
import java.util.Collection;
import javax.xml.transform.TransformerException;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class builds a SAML Attribute-Response statement.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class AttributeResponseBuilder extends ResponseBuilder {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(AttributeResponseBuilder.class);
    private static final StiamConfiguration config = StiamConfiguration.getInstance();

    private final Collection<Attribute> attributes;
    private final String nameId;

//////////////////////////////////////// Constructors
    /**
     * Initializes a builder for attribute-responses with the given attributes.
     *
     * @param destination The recipient of the response
     * @param inResponseTo ID from the requesting extended responseAttribute
     * query
     * @param nameId NameID of the subject belonging to the attributes
     * @param attributes The attributes to include in the response.An
     * responseAttribute consists of a String[] {name, nameformat, friendlyname}
     * @throws org.opensaml.xml.ConfigurationException
     * @throws java.security.NoSuchAlgorithmException
     */
    public AttributeResponseBuilder(String destination, String inResponseTo,
            String nameId, Collection<Attribute> attributes) throws ConfigurationException, NoSuchAlgorithmException {
        super(destination, inResponseTo, new String[]{STATUS_CODE_SUCCESS});
        this.nameId = nameId;
        this.attributes = attributes;
    }

    /**
     * Overrides the method in ResponseBuilder in order to generate a full SAML
     * attribute response.
     *
     * @return A marshalled version of the attribute response
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
        logger.debug("Starting generation of attribute response...");
        final Response response = buildRawResponse();

        //////////////////// Add attribute assertion to the response
        final Assertion assertion = (Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setID(messageId);
        assertion.setIssueInstant(issueInstant);
        response.getAssertions().add(assertion);

        // --> Issuer of the Assertion
        final Issuer assertionIssuer = (Issuer) buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        assertionIssuer.setValue(config.getSAMLIssuer());
        assertion.setIssuer(assertionIssuer);

        // --> Subject of the Assertion
        final Subject assertionSubject = (Subject) buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
        assertion.setSubject(assertionSubject);

        // --> --> NameID of the Subject
        final NameID assertionNameID = (NameID) buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        assertionNameID.setFormat(config.getSAMLNameIDFormat());
        assertionNameID.setValue(nameId);
        assertionSubject.setNameID(assertionNameID);

        // --> Conditions for the Assertion
        final Conditions conditions = (Conditions) buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(issueInstant);
        conditions.setNotOnOrAfter(issueInstant.plusMinutes(config.getSAMLAssertionValidityMinutes()));
        assertion.setConditions(conditions);

        // --> --> AudienceRestriction for the Conditions
        final AudienceRestriction audienceRestriction = (AudienceRestriction) buildXMLObject(
                AudienceRestriction.DEFAULT_ELEMENT_NAME);
        conditions.getAudienceRestrictions().add(audienceRestriction);

        // --> --> --> Audience for the AudienceRestriction
        final Audience audience = (Audience) buildXMLObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(destination);
        audienceRestriction.getAudiences().add(audience);

        // --> AttributeStatement of the Assertion
        final AttributeStatement attributeStatement = (AttributeStatement) buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
        assertion.getAttributeStatements().add(attributeStatement);

        // --> --> Attributes of the AttributeStatement
        for (Attribute attribute : attributes) {
            org.opensaml.saml2.core.Attribute responseAttribute = (org.opensaml.saml2.core.Attribute) buildXMLObject(org.opensaml.saml2.core.Attribute.DEFAULT_ELEMENT_NAME);
            responseAttribute.setName(attribute.getName());
            if (attribute.getFriendlyName() != null) {
                responseAttribute.setFriendlyName(attribute.getFriendlyName());
            }
            if (attribute.getNameFormat() != null) {
                responseAttribute.setNameFormat(attribute.getNameFormat());
            }
            if (attribute.getValue() != null) {
                XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
                XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                stringValue.setValue(attribute.getValue());
                responseAttribute.getAttributeValues().add(stringValue);
            }
            attributeStatement.getAttributes().add(responseAttribute);
        }

        //////////////////// Sign the assertion
        final Credential signingCredential = StiamConfiguration.getInstance().getSignatureCredential();
        Signature assertionSignature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        assertionSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(assertionSignature, signingCredential, null, null);
        assertion.setSignature(assertionSignature);
        Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(assertionSignature);

        //////////////////// Sign the response
        final Signature responseSignature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        responseSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(responseSignature, signingCredential, null, null);
        response.setSignature(responseSignature);
        Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(responseSignature);

        logger.debug("Attribute response generated!");
        return marshallToString(response);
    }
}
