/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.xml.security.credential.Credential;

/**
 * Testsuite for the StiamConfiguration
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class StiamConfigurationTest {
//////////////////////////////////////// Fields

    private static StiamConfiguration stiamConfig;
    private static TestConfiguration testConfig;

    // File paths
    private static final String CONFIG_DIR = ".stiam-aa";
    private static final String DEFAULT_ATTRIBUTENAMEMAPPER_FILEPATH = CONFIG_DIR + "/attributename-mapper.properties";
    private static final String DEFAULT_ELIGIBILITYCHECKER_FILEPATH = CONFIG_DIR + "/eligibility-checker.properties";
    private static final String DEFAULT_QUERYSENDER_FILEPATH = CONFIG_DIR + "/query-sender.properties";
    private static final String DEFAULT_PROPERTYDIRECTORY_FILEPATH = CONFIG_DIR + "/property-directory.properties";
    private static final String DEFAULT_TESTCONFIGURATION_FILEPATH = CONFIG_DIR + "/aa-test.properties";
    private static final String DEFAULT_KEYSTORE_FILEPATH = CONFIG_DIR + "/aa.jks";

    // SAML default settings
    private static final String DEFAULT_SAML_ISSUER = "https://aa.example.org";
    private static final String DEFAULT_SAML_DESTINATION = "http://localhost:8080/";
    private static final String DEFAULT_SAML_ASSERTIONVALIDITYMINUTES = "10";
    private static final String DEFAULT_SAML_NAMEID = "johndoe";
    private static final String DEFAULT_SAML_NAMEIDFORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified";
    private static final String DEFAULT_SAML_ASSURANCELEVEL = "http://assurance.example.org/level1";
    private static final String DEFAULT_SAML_SUBJECTCONFIRMATIONMETHOD = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
    private static final String DEFAULT_SAML_RETURN_POST_BINDING = "true";

    // LDAP default settings
    private static final String DEFAULT_LDAP_HOST = "localhost";
    private static final String DEFAULT_LDAP_PORT = "389";
    private static final String DEFAULT_LDAP_USER = "cn=manager";
    private static final String DEFAULT_LDAP_PASSPHRASE = "";
    private static final String DEFAULT_LDAP_BASEDN = "";
    private static final String DEFAULT_LDAP_FILTER = "(uid=%s)";

    // Attribute-Service configuration
    private static final String DEFAULT_BINDING = "soap";
    private static final String DEFAULT_ATTRIBUTEQUERY_ENCODING = "UTF-8";
    private static final String DEFAULT_VERIFY_QUERY_SIGNATURE = "true";
    private static final String DEFAULT_VERIFY_AUTHN_STATEMENT = "true";
    private static final String DEFAULT_VERIFY_AUTHN_SIGNATURE = "true";
    private static final String DEFAULT_VERIFY_AUTHN_TIMESPAN = "true";

    // Keystore configuration
    private static final String DEFAULT_KEYSTORE_PASSWORD = "secret";
    private static final String DEFAULT_KEYSTORE_ENTRY_NAME = "stiam-aa";
    private static final String DEFAULT_KEYSTORE_ENTRY_PASSWORD = "secret";

    // Other default settings
    private static final String DEFAULT_ELIGIBILITY_POLICY = "deny";
    private static final String DEFAULT_DIRECTORY = "property";

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setupStiamConfiguration() {
        stiamConfig = StiamConfiguration.getInstance();
        testConfig = new TestConfiguration();
    }

    ////////////////////Filepath-Methods
    @Test
    public void tryToGetAttributeMapperFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.AttributeMapper.FilePath", System.getProperties().getProperty("user.home")
                + "/" + DEFAULT_ATTRIBUTENAMEMAPPER_FILEPATH), stiamConfig.getAttributeNameMapperFilePath());
    }

    @Test
    public void tryToGetEligibilityCheckerFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.EligibilityChecker.FilePath", System.getProperties().getProperty("user.home")
                + "/" + DEFAULT_ELIGIBILITYCHECKER_FILEPATH), stiamConfig.getEligibilityCheckerFilePath());
    }

    @Test
    public void tryToGetQuerySenderFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.QuerySender.FilePath", System.getProperties().getProperty("user.home")
                + "/" + DEFAULT_QUERYSENDER_FILEPATH), stiamConfig.getQuerySenderFilePath());
    }

    @Test
    public void tryToGetPropertyDirectoryFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.PropertyDirectory.FilePath", System.getProperties().getProperty("user.home")
                + "/" + DEFAULT_PROPERTYDIRECTORY_FILEPATH), stiamConfig.getPropertyDirectoryFilePath());
    }

    @Test
    public void tryToGetTestConfigurationFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.TestConfiguration.FilePath", System.getProperties().getProperty("user.home")
                + "/" + DEFAULT_TESTCONFIGURATION_FILEPATH), stiamConfig.getTestConfigurationFilePath());
    }

    @Test
    public void tryToGetKeystoreFilePath() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.Keystore.FilePath", System.getProperties().getProperty("user.home") + "/"
                + DEFAULT_KEYSTORE_FILEPATH), stiamConfig.getKeystoreFilePath());
    }

    //////////////////// SAML-Methods
    @Test
    public void tryToGetSAMLIssuer() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.Issuer", DEFAULT_SAML_ISSUER),
                stiamConfig.getSAMLIssuer());
    }

    @Test
    public void tryToGetSAMLDestination() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.Destination", DEFAULT_SAML_DESTINATION),
                stiamConfig.getSAMLDestination());
    }

    @Test
    public void tryToGetSAMLAssertionValidityMinutes() {
        assertEquals(Integer.parseInt(testConfig.getProperty("StiamConfigurationTest.SAML.AssertionValidityMinutes", DEFAULT_SAML_ASSERTIONVALIDITYMINUTES)),
                stiamConfig.getSAMLAssertionValidityMinutes());
    }

    @Test
    public void tryToGetSAMLNameID() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.NameID", DEFAULT_SAML_NAMEID),
                stiamConfig.getSAMLNameID());
    }

    @Test
    public void tryToGetSAMLNameIDFormat() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.NameIDFormat", DEFAULT_SAML_NAMEIDFORMAT),
                stiamConfig.getSAMLNameIDFormat());
    }

    @Test
    public void tryToGetSAMLAssuranceLevel() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.AssuranceLevel", DEFAULT_SAML_ASSURANCELEVEL),
                stiamConfig.getSAMLAssuranceLevel());
    }

    @Test
    public void tryToGetSAMLSubjectConfirmationMethod() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.SAML.SubjectConfirmationMethod", DEFAULT_SAML_SUBJECTCONFIRMATIONMETHOD),
                stiamConfig.getSAMLSubjectConfirmationMethod());
    }

    //////////////////// LDAP-Methods
    @Test
    public void tryToGetLdapHost() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.LdapHost", DEFAULT_LDAP_HOST),
                stiamConfig.getLdapHost());
    }

    @Test
    public void tryToGetLdapPort() {
        assertEquals(Integer.parseInt(testConfig.getProperty("StiamConfigurationTest.LdapPort", DEFAULT_LDAP_PORT)),
                stiamConfig.getLdapPort());
    }

    @Test
    public void tryToGetLdapUser() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.LdapUser", DEFAULT_LDAP_USER), stiamConfig.getLdapUser());
    }

    @Test
    public void tryToGetLdapPassphrase() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.LdapPassphrase", DEFAULT_LDAP_PASSPHRASE), stiamConfig.getLdapPassphrase());
    }

    @Test
    public void tryToGetLdapBaseDN() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.LdapBaseDN", DEFAULT_LDAP_BASEDN), stiamConfig.getLdapBaseDN());
    }

    @Test
    public void tryToGetLdapFilter() {
        String testNameId = "johnd";
        assertEquals(String.format(testConfig.getProperty("StiamConfigurationTest.LdapFilter", DEFAULT_LDAP_FILTER), testNameId),
                stiamConfig.getLdapFilter(testNameId));
    }

    //////////////////// Attribute-Service configuration
    @Test
    public void tryToGetBinding() {
        if (testConfig.getProperty("StiamConfigurationTest.Binding", DEFAULT_BINDING).equalsIgnoreCase("http_post")) {
            assertEquals(stiamConfig.getBinding(), StiamConfiguration.Binding.HTTP_POST);
        } else {
            assertEquals(stiamConfig.getBinding(), StiamConfiguration.Binding.SOAP);
        }
    }

    @Test
    public void tryToGetAttributeQueryEncoding() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.AttributeQueryEncoding", DEFAULT_ATTRIBUTEQUERY_ENCODING),
                stiamConfig.getAttributeQueryEncoding());
    }

    @Test
    public void tryToVerifyQuerySignature() {
        assertEquals(Boolean.parseBoolean(testConfig.getProperty("StiamConfigurationTest.VerifyQuerySignature", DEFAULT_VERIFY_QUERY_SIGNATURE)),
                stiamConfig.verifyQuerySignature());
    }

    @Test
    public void tryToVerifyAuthnStatement() {
        assertEquals(Boolean.parseBoolean(testConfig.getProperty("StiamConfigurationTest.VerifyAuthnStatement", DEFAULT_VERIFY_AUTHN_STATEMENT)),
                stiamConfig.verifyAuthnStatement());
    }

    @Test
    public void tryToVerifyAuthnSignature() {
        assertEquals(Boolean.parseBoolean(testConfig.getProperty("StiamConfigurationTest.VerifyAuthnSignature", DEFAULT_VERIFY_AUTHN_SIGNATURE)),
                stiamConfig.verifyAuthnSignature());
    }

    @Test
    public void tryToVerifyAuthnTimespan() {
        assertEquals(Boolean.parseBoolean(testConfig.getProperty("StiamConfigurationTest.VerifyAuthnTimespan", DEFAULT_VERIFY_AUTHN_TIMESPAN)),
                stiamConfig.verifyAuthnTimespan());
    }

    //////////////////// Credential configuration
    @Test
    public void tryToGetKeystorePassword() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.KeystorePassword", DEFAULT_KEYSTORE_PASSWORD),
                stiamConfig.getKeystorePassword());
    }

    @Test
    public void tryToGetKeystoreEntryName() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.KeystoreEntryName", DEFAULT_KEYSTORE_ENTRY_NAME),
                stiamConfig.getKeystoreEntryName());
    }

    @Test
    public void tryToGetKeystoreEntryPassword() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.KeystoreEntryPassword", DEFAULT_KEYSTORE_ENTRY_PASSWORD),
                stiamConfig.getKeystoreEntryPassword());
    }

    @Test
    public void tryToGetCredential() throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        final Credential credential = stiamConfig.getCredential(testConfig.getProperty("StiamConfigurationTest.KeystoreEntryName", DEFAULT_KEYSTORE_ENTRY_NAME));
        Assert.assertNotNull(credential);
    }

    @Test
    public void tryToGetSignatureCredential() throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        final Credential credential = stiamConfig.getSignatureCredential();
        Assert.assertNotNull(credential);
    }

    @Test
    public void tryToGetVerificationCredential() throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        final Credential credential = stiamConfig.getVerificationCredential();
        Assert.assertNotNull(credential);
    }

    ////////////////////Various
    @Test
    public void tryToGetDefaultEligibilityPolicy() {
        boolean eligibility = false;

        if (testConfig.getProperty("StiamConfigurationTest.DefaultEligibilityPolicy", DEFAULT_ELIGIBILITY_POLICY).equalsIgnoreCase("accept")) {
            eligibility = true;
        }

        assertEquals(eligibility, stiamConfig.getDefaultEligibilityPolicy());
    }

    public void tryToGetDirectory() {
        assertEquals(testConfig.getProperty("StiamConfigurationTest.Directory", DEFAULT_DIRECTORY), stiamConfig.getDirectory());
    }
}
