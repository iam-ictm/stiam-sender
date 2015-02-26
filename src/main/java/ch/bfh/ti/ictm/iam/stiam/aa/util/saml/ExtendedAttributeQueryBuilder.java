/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util.saml;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
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
 * This class builds an extended SAML attribute query with included
 * authentication assertion according to the specification.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class ExtendedAttributeQueryBuilder extends MessageBuilder {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(ExtendedAttributeQueryBuilder.class);
    private static final StiamConfiguration config = StiamConfiguration.getInstance();
    private final List<String[]> attributes;

//////////////////////////////////////// Constructors
    /**
     * Initializes a builder for extended attribute queries with the given
     * attributes.
     *
     * @param attributes The attributes to include in the query.An attribute
     * consists of a String[] {name, nameformat, friendlyname}
     */
    public ExtendedAttributeQueryBuilder(List<String[]> attributes) {
        this.attributes = attributes;
    }

//////////////////////////////////////// Methods
    /**
     * Builds a complete, extended SAML attribute query with included
     * authentication statement, both of them signed, serialized to String.
     * Overrides the method from MessageBuilder.
     *
     * @return Serialized extended attribute query as String
     * @throws ConfigurationException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     * @throws SecurityException
     * @throws MarshallingException
     * @throws SignatureException
     * @throws TransformerException
     * @throws XMLParserException
     */
    @Override
    public String build() throws ConfigurationException,
            NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException,
            UnrecoverableEntryException, SecurityException, MarshallingException,
            SignatureException, XMLParserException, TransformerException {
        logger.debug("Starting generation of extended attribute query...");

        //////////////////// Perform initial setup
        DefaultBootstrap.bootstrap();
        final SecureRandomIdentifierGenerator idGenerator = new SecureRandomIdentifierGenerator();

        final DateTime queryTime = DateTime.now();
        final DateTime authnIssueTime = queryTime.minusMinutes(4);
        final DateTime authnNotBeforeTime = queryTime.minusMinutes(5);
        final DateTime authnNotAfterTime = queryTime.plusMinutes(5);

        //////////////////// The outer AttributeQuery
        final AttributeQuery query = (AttributeQuery) buildXMLObject(AttributeQuery.DEFAULT_ELEMENT_NAME);
        query.setID(idGenerator.generateIdentifier());
        query.setVersion(SAMLVersion.VERSION_20);
        query.setIssueInstant(queryTime);
        query.setSubject(null);
        query.setDestination(config.getSAMLDestination());

        // Issuer of the AttributeQuery
        final Issuer queryIssuer = (Issuer) buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        queryIssuer.setValue(config.getSAMLIssuer());
        query.setIssuer(queryIssuer);

        // Subject of the AttributeQuery
        final Subject querySubject = (Subject) buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
        query.setSubject(querySubject);

        // --> NameID of the Subject
        NameID queryNameID = (NameID) buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        queryNameID.setFormat(config.getSAMLNameIDFormat());
        queryNameID.setValue(config.getSAMLNameID());
        querySubject.setNameID(queryNameID);

        // Attributes of the AttributeQuery
        for (String[] attr : attributes) {
            Attribute attribute = (Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
            attribute.setName(attr[0]);
            if (attr.length >= 2) {
                attribute.setNameFormat(attr[1]);
            }
            if (attr.length >= 3) {
                attribute.setFriendlyName(attr[2]);
            }
            query.getAttributes().add(attribute);
        }

        //////////////////// The Assertion added to the Extensions of the above query
        Assertion assertion = (Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID(idGenerator.generateIdentifier());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssueInstant(authnIssueTime);

        // --> Issuer of the Assertion
        Issuer assertionIssuer = (Issuer) buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        assertionIssuer.setValue(config.getSAMLIssuer());
        assertion.setIssuer(assertionIssuer);

        // --> Subject of the Assertion
        Subject assertionSubject = (Subject) buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
        assertion.setSubject(assertionSubject);

        // --> -->  NameID for the Subject of the Assertion
        NameID assertionNameID = (NameID) buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        assertionNameID.setFormat(config.getSAMLNameIDFormat());
        assertionNameID.setValue(config.getSAMLNameID());
        assertionSubject.setNameID(assertionNameID);

        // --> -->  SubjectConfirmation for the Subject of the Assertion
        SubjectConfirmation assertionSubjectConfirmation = (SubjectConfirmation) buildXMLObject(
                SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        assertionSubjectConfirmation.setMethod(config.getSAMLSubjectConfirmationMethod());
        assertionSubject.getSubjectConfirmations().add(assertionSubjectConfirmation);

        // --> Conditions for the Assertion
        Conditions conditions = (Conditions) buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(authnNotBeforeTime);
        conditions.setNotOnOrAfter(authnNotAfterTime);
        assertion.setConditions(conditions);

        // --> --> AudienceRestriction for the Conditions
        AudienceRestriction audienceRestriction = (AudienceRestriction) buildXMLObject(
                AudienceRestriction.DEFAULT_ELEMENT_NAME);
        conditions.getAudienceRestrictions().add(audienceRestriction);

        // --> --> --> Audience for the AudienceRestriction
        Audience audience = (Audience) buildXMLObject(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(config.getSAMLIssuer());
        audienceRestriction.getAudiences().add(audience);

        // --> AuthnStatement for the Assertion
        AuthnStatement authnStatement = (AuthnStatement) buildXMLObject(
                AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnInstant(authnIssueTime);
        assertion.getAuthnStatements().add(authnStatement);

        // -->-->  AuthnContext for the AuthnStatement
        AuthnContext authnContext = (AuthnContext) buildXMLObject(AuthnContext.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext(authnContext);

        // --> -->-->  AuthnContextClassRef for AuthnContext
        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) buildXMLObject(
                AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(config.getSAMLAssuranceLevel());
        authnContext.setAuthnContextClassRef(authnContextClassRef);

        //////////////////// Sign the assertion and add it to the query
        Credential signingCredential = StiamConfiguration.getInstance().getSignatureCredential();
        Signature assertionSignature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        assertionSignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(assertionSignature, signingCredential, null, null);
        assertion.setSignature(assertionSignature);
        Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(assertionSignature);

        // Extensions of the AttributeQuery
        // Manually build the correct QName, otherwise "md"-namespace gets marshalled...
        // see https://groups.google.com/forum/#!topic/opensaml-users/FFCQ48uqw3o for details.
        QName name = new QName(SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME,
                SAMLConstants.SAML20P_PREFIX);
        Extensions extensions = (Extensions) Configuration.getBuilderFactory().getBuilder(name)
                .buildObject(name);
        extensions.getUnknownXMLObjects().add(assertion);
        query.setExtensions(extensions);

        //////////////////// Sign the query
        Signature querySignature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        querySignature.setSigningCredential(signingCredential);
        SecurityHelper.prepareSignatureParams(querySignature, signingCredential, null, null);
        query.setSignature(querySignature);
        Configuration.getMarshallerFactory().getMarshaller(query).marshall(query);
        Signer.signObject(querySignature);

        logger.debug("Extended attribute query generated!");
        return marshallToString(query);
    }
}
