/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Configuration-object for the main configuration of the AA. Configuration is
 * initialized by reading a properties file whose location defaults to user.home
 * + DEFAULT_CONFIG_DIR + DEFAULT_CONFIG_FILEPATH (normally
 * ~/.stiam-aa/aa.properties).
 *
 * The location of the configuration-file can be overridden with the system
 * property "stiam.config".
 *
 * If no configuration can be loaded, default values will be used which most
 * probably will not lead to useful results...
 *
 * This class is a singleton.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class StiamConfiguration {
//////////////////////////////////////// Fields    

    // Main configuration
    /**
     * Default path-fragment with the directory-name of the directory containing
     * all configuration of STIAM-AA
     */
    protected static final String DEFAULT_CONFIG_DIR = ".stiam-aa";

    // File paths of other configuration files
    /**
     * This and all subsequent, following DEFAULT_*-constants hold default
     * values for their respective configuration options. They are applied if no
     * value for the specific property can be found in the main configuration
     * file.
     */
    protected static final String DEFAULT_CONFIG_FILEPATH = DEFAULT_CONFIG_DIR + "/aa.properties";
    protected static final String DEFAULT_ATTRIBUTENAMEMAPPER_FILEPATH = DEFAULT_CONFIG_DIR + "/attributename-mapper.properties";
    protected static final String DEFAULT_ELIGIBILITYCHECKER_FILEPATH = DEFAULT_CONFIG_DIR + "/eligibility-checker.properties";
    protected static final String DEFAULT_QUERYSENDER_FILEPATH = DEFAULT_CONFIG_DIR + "/query-sender.properties";
    protected static final String DEFAULT_PROPERTYDIRECTORY_FILEPATH = DEFAULT_CONFIG_DIR + "/property-directory.properties";
    protected static final String DEFAULT_TESTCONFIGURATION_FILEPATH = DEFAULT_CONFIG_DIR + "/aa-test.properties";
    protected static final String DEFAULT_KEYSTORE_FILEPATH = DEFAULT_CONFIG_DIR + "/aa.jks";

    // SAML default settings
    protected static final String DEFAULT_SAML_ISSUER = "https://aa.example.org";
    protected static final String DEFAULT_SAML_DESTINATION = "http://localhost:8080/";
    protected static final String DEFAULT_SAML_ASSERTIONVALIDITYMINUTES = "10";
    protected static final String DEFAULT_SAML_NAMEID = "johndoe";
    protected static final String DEFAULT_SAML_NAMEIDFORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified";
    protected static final String DEFAULT_SAML_ASSURANCELEVEL = "http://assurance.example.org/level1";
    protected static final String DEFAULT_SAML_SUBJECTCONFIRMATIONMETHOD = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
    protected static final String DEFAULT_SAML_RETURN_POST_BINDING = "true";

    // LDAP default settings
    protected static final String DEFAULT_LDAP_HOST = "localhost";
    protected static final String DEFAULT_LDAP_PORT = "389";
    protected static final String DEFAULT_LDAP_USER = "cn=manager";
    protected static final String DEFAULT_LDAP_PASSPHARSE = "";
    protected static final String DEFAULT_LDAP_BASEDN = "";
    protected static final String DEFAULT_LDAP_FILTER = "(uid=%s)";

    // Attribute-Service configuration
    protected static final String DEFAULT_ATTRIBUTEQUERY_ENCODING = "UTF-8";
    protected static final String DEFAULT_VERIFY_QUERY_SIGNATURE = "true";
    protected static final String DEFAULT_VERIFY_AUTHN_STATEMENT = "true";
    protected static final String DEFAULT_VERIFY_AUTHN_SIGNATURE = "true";
    protected static final String DEFAULT_VERIFY_AUTHN_TIMESPAN = "true";

    // Keystore configuration
    protected static final String DEFAULT_KEYSTORE_PASSWORD = "secret";
    protected static final String DEFAULT_KEYSTORE_ENTRY_NAME = "stiam-aa";
    protected static final String DEFAULT_KEYSTORE_ENTRY_PASSWORD = "secret";

    // Other default settings
    protected static final String DEFAULT_ELEGIBILITY_POLICY = "deny";
    protected static final String DEFAULT_DIRECTORY = "property";

    // Instance variables
    private static final Logger logger = getLogger(StiamConfiguration.class);
    private static final StiamConfiguration instance = new StiamConfiguration();
    private static Properties stiamSettings;
    private static String userHome;

//////////////////////////////////////// Constructors
    /**
     * Try to load the configuration. See description of this class for more
     * information about how the configuration is loaded.
     */
    private StiamConfiguration() {
        Properties systemProperties = System.getProperties();
        userHome = systemProperties.getProperty("user.home");
        if (systemProperties.getProperty("stiam.config") != null
                && !systemProperties.getProperty("stiam.config").isEmpty()) {
            logger.info("Trying to read configuration specified by system property: {}", systemProperties.getProperty("stiam.config"));
            loadConfig(systemProperties.getProperty("stiam.config"));
        } else {
            logger.info("Trying to read configuration file from default location");
            loadConfig(userHome + "/" + DEFAULT_CONFIG_FILEPATH);
        }

        if (stiamSettings == null) {
            logger.warn("Configuration could not be loaded, running with default values. That will probably lead to strange results...");
        }
    }

//////////////////////////////////////// Public Methods
    /**
     * @return The one and only instance of this class. (Singleton)
     */
    public static StiamConfiguration getInstance() {
        return instance;
    }

//////////////////// Filepath-Methods
    /**
     * Defines the path to the properties-file with the configuration for the
     * AttributeNameMapper
     *
     * Can be configured with property "AttributeNameMapper.FilePath", defaults
     * to DEFAULT_ATTRIBUTENAMEMAPPER_FILEPATH
     *
     * @return Path to the properties-file
     */
    public String getAttributeNameMapperFilePath() {
        return stiamSettings.getProperty("AttributeNameMapper.FilePath", userHome + "/" + DEFAULT_ATTRIBUTENAMEMAPPER_FILEPATH);
    }

    /**
     * Defines the path to the properties-file with the configuration for the
     * EligibilityChecker
     *
     * Can be configured with property "EligibilityChecker.FilePath", defaults
     * to DEFAULT_ELIGIBILITYCHECKER_FILEPATH
     *
     * @return Path to the properties-file
     */
    public String getEligibilityCheckerFilePath() {
        return stiamSettings.getProperty("EligibilityChecker.FilePath", userHome + "/" + DEFAULT_ELIGIBILITYCHECKER_FILEPATH);
    }

    /**
     * Defines the path to the properties-file with the configuration for the
     * QuerySender
     *
     * Can be configured with property "QuerySender.FilePath", defaults to
     * DEFAULT_QUERYSENDER_FILEPATH
     *
     * @return Path to the properties-file
     */
    public String getQuerySenderFilePath() {
        return stiamSettings.getProperty("QuerySender.FilePath", userHome + "/" + DEFAULT_QUERYSENDER_FILEPATH);
    }

    /**
     * Defines the path to the properties-file with the configuration for the
     * PropertyDirectory
     *
     * Can be configured with property "PropertyDirectory.FilePath", defaults to
     * DEFAULT_PROPERTYDIRECTORY_FILEPATH
     *
     * @return Path to the properties-file
     */
    public String getPropertyDirectoryFilePath() {
        return stiamSettings.getProperty("PropertyDirectory.FilePath", userHome + "/" + DEFAULT_PROPERTYDIRECTORY_FILEPATH);
    }

    /**
     * Defines the path to the properties-file with the configuration for the
     * unit-tests
     *
     * Can be configured with property "TestConfiguration.FilePath", defaults to
     * DEFAULT_TESTCONFIGURATION_FILEPATH
     *
     * @return Path to the properties-file
     */
    public String getTestConfigurationFilePath() {
        return stiamSettings.getProperty("TestConfiguration.FilePath", userHome + "/" + DEFAULT_TESTCONFIGURATION_FILEPATH);
    }

    /**
     * Defines the path to the keystore-file containing the credentials used for
     * signing and verifying.
     *
     * Can be configured with property "Keystore.FilePath", defaults to
     * DEFAULT_KEYSTORE_FILEPATH
     *
     * @return Path to keystore-file
     */
    public String getKeystoreFilePath() {
        return stiamSettings.getProperty("Keystore.FilePath", userHome + "/" + DEFAULT_KEYSTORE_FILEPATH);
    }

//////////////////// LDAP-Methods
    /**
     * Defines the Issuer used when generating SAML-messages.
     *
     * Can be configured with property "SAML.Issuer", defaults to
     * DEFAULT_SAML_ISSUER
     *
     * @return Issuer used in SAML-messages
     */
    public String getSAMLIssuer() {
        return stiamSettings.getProperty("SAML.Issuer", DEFAULT_SAML_ISSUER);
    }

    /**
     * Defines the Destination used when generating SAML-messages.
     *
     * Can be configured with property "SAML.Destination", defaults to
     * DEFAULT_SAML_DESTINATION
     *
     * @return Destination used in SAML-messages
     */
    public String getSAMLDestination() {
        return stiamSettings.getProperty("SAML.Destination", DEFAULT_SAML_DESTINATION);
    }

    /**
     * Defines the validity period in minutes of generated SAML-messages.
     *
     * Can be configured with property "SAML.AssertionValidityMinutes", defaults
     * to DEFAULT_SAML_ASSERTIONVALIDITYMINUTES
     *
     * @return Validity in minutes used in SAML-messages
     */
    public int getSAMLAssertionValidityMinutes() {
        return Integer.parseInt(stiamSettings.getProperty("SAML.AssertionValidityMinutes", DEFAULT_SAML_ASSERTIONVALIDITYMINUTES));
    }

    /**
     * Defines the NameID used when generating SAML-messages.
     *
     * Can be configured with property "SAML.NameID", defaults to
     * DEFAULT_SAML_NAMEID
     *
     * @return NameID used in SAML-messages
     */
    public String getSAMLNameID() {
        return stiamSettings.getProperty("SAML.NameID", DEFAULT_SAML_NAMEID);
    }

    /**
     * Defines the NameID-format used when generating SAML-messages.
     *
     * Can be configured with property "SAML.NameIDFormat", defaults to
     * DEFAULT_SAML_NAMEIDFORMAT
     *
     * @return NameID-format used in SAML-messages
     */
    public String getSAMLNameIDFormat() {
        return stiamSettings.getProperty("SAML.NameIDFormat", DEFAULT_SAML_NAMEIDFORMAT);
    }

    /**
     * Defines the assurance-level used when generating SAML-messages.
     *
     * Can be configured with property "SAML.AssuranceLevel", defaults to
     * DEFAULT_SAML_ASSURANCELEVEL
     *
     * @return Assurance-level used in SAML-messages
     */
    public String getSAMLAssuranceLevel() {
        return stiamSettings.getProperty("SAML.AssuranceLevel", DEFAULT_SAML_ASSURANCELEVEL);
    }

    /**
     * Defines the subject-confirmation-method used when generating
     * SAML-messages.
     *
     * Can be configured with property "SAML.SubjectConfirmationMethod",
     * defaults to DEFAULT_SAML_SUBJECTCONFIRMATIONMETHOD
     *
     * @return Confirmation-method of the subject used in SAML-messages
     */
    public String getSAMLSubjectConfirmationMethod() {
        return stiamSettings.getProperty("SAML.SubjectConfirmationMethod", DEFAULT_SAML_SUBJECTCONFIRMATIONMETHOD);
    }

    /**
     * Defines the format of the responses returned by the AA. If true, a
     * response according to SAML2.0 HTTP-POST-binding is generated, if false a
     * simple plaintext response is generated.
     *
     * Can be configured with property "SAML.ReturnPOSTBinding", defaults to
     * DEFAULT_SAML_RETURN_POST_BINDING
     *
     * @return Confirmation-method of the subject used in SAML-messages
     */
    public boolean getSAMLReturnPOSTBinding() {
        return Boolean.parseBoolean(stiamSettings.getProperty("SAML.ReturnPOSTBinding", DEFAULT_SAML_RETURN_POST_BINDING));
    }

//////////////////// LDAP-Methods
    /**
     * Defines which host is used to access the LDAP directory.
     *
     * Can be configured with property "LdapClient.Host", defaults to
     * DEFAULT_LDAP_HOST.
     *
     * @return Host of the LDAP directory.
     */
    public String getLdapHost() {
        return stiamSettings.getProperty("LdapClient.Host", DEFAULT_LDAP_HOST);
    }

    /**
     * Defines which port is used to access the LDAP directory.
     *
     * Can be configured with property "LdapClient.Port", defaults to
     * DEFAULT_LDAP_HOST.
     *
     * @return Port of the LDAP directory.
     */
    public int getLdapPort() {
        return Integer.parseInt(stiamSettings.getProperty("LdapClient.Port", DEFAULT_LDAP_PORT));
    }

    /**
     * Defines the user accessing the LDAP directory.
     *
     * Can be configured with property "LdapClient.User", defaults to
     * DEFAULT_LDAP_USER.
     *
     * @return User accessing the LDAP directory.
     */
    public String getLdapUser() {
        return stiamSettings.getProperty("LdapClient.User", DEFAULT_LDAP_USER);
    }

    /**
     * Defines the passphrase of the user accessing the LDAP directory.
     *
     * Can be configured with property "LdapClient.Passphrase", defaults to
     * DEFAULT_LDAP_PASSPHARSE.
     *
     * @return Passphrase of the LDAP user.
     */
    public String getLdapPassphrase() {
        return stiamSettings.getProperty("LdapClient.Passphrase", DEFAULT_LDAP_PASSPHARSE);
    }

    /**
     * Defines the BaseDN used by the LDAP-Client.
     *
     * Can be configured with property "LdapClient.BaseDN", defaults to
     * DEFAULT_LDAP_BASEDN.
     *
     * @return BaseDN used by the LDAP-Client.
     */
    public String getLdapBaseDN() {
        return stiamSettings.getProperty("LdapClient.BaseDN", DEFAULT_LDAP_BASEDN);
    }

    /**
     * Defines the search filter used by the LDAP-Client.
     *
     * Can be configured with property "LdapClient.Filter", defaults to
     * DEFAULT_LDAP_FILTER.
     *
     * @param nameId The nameId is used to create the filter.
     * @return Filter used by the LDAP-Client.
     */
    public String getLdapFilter(String nameId) {
        return String.format(stiamSettings.getProperty("LdapClient.Filter", DEFAULT_LDAP_FILTER), nameId);
    }

//////////////////// Attribute-Service configuration
    /**
     * Defines which encoding is used in the SAML-messages
     *
     * Can be configured with property
     * "AttributeService.AttributeQueryEncoding", defaults to
     * DEFAULT_ATTRIBUTEQUERY_ENCODING.
     *
     * @return Encoding to use
     */
    public String getAttributeQueryEncoding() {
        return stiamSettings.getProperty("AttributeService.AttributeQueryEncoding", DEFAULT_ATTRIBUTEQUERY_ENCODING);
    }

    /**
     * Enables/disables verification of the signature of the attribute-query.
     *
     * Can be configured with property "AttributeService.VerifyQuerySignature",
     * defaults to DEFAULT_VERIFY_QUERY_SIGNATURE.
     *
     * @return if true, signature of the attribute-query is verified.
     */
    public boolean verifyQuerySignature() {
        return Boolean.parseBoolean(stiamSettings.getProperty("AttributeService.VerifyQuerySignature", DEFAULT_VERIFY_QUERY_SIGNATURE));
    }

    /**
     * Defines if the AA tries to verify an authentication statement included in
     * the extensions.
     *
     * Can be configured with property "AttributeService.VerifyAuthnStatement",
     * defaults to DEFAULT_VERIFY_AUTHN_STATEMENT.
     *
     * @return if true, Authn-statement is verified.
     */
    public boolean verifyAuthnStatement() {
        return Boolean.parseBoolean(stiamSettings.getProperty("AttributeService.VerifyAuthnStatement", DEFAULT_VERIFY_AUTHN_STATEMENT));
    }

    /**
     * Enables/disables verification of the signature of the
     * authentication-statment. Note: signature gets only verified if
     * VerifyAuthnStatement is true.
     *
     * Can be configured with property "AttributeService.VerifyAuthnSignature",
     * defaults to DEFAULT_VERIFY_AUTHN_SIGNATURE.
     *
     * @return if true, signature of the authentication-statment is verified.
     */
    public boolean verifyAuthnSignature() {
        return Boolean.parseBoolean(stiamSettings.getProperty("AttributeService.VerifyAuthnSignature", DEFAULT_VERIFY_AUTHN_SIGNATURE));
    }

    /**
     * Enables/disables verification of conditions-timespan ("NotBefore" and
     * "NotOnOrAfter") of the authentication-statement. Note: timespan gets only
     * verified if VerifyAuthnStatement is true.
     *
     * Can be configured with property "AttributeService.VerifyAuthnTimespan",
     * defaults to DEFAULT_VERIFY_AUTHN_TIMESPAN.
     *
     * @return if true, signature of the authentication-statment is verified.
     */
    public boolean verifyAuthnTimespan() {
        return Boolean.parseBoolean(stiamSettings.getProperty("AttributeService.VerifyAuthnTimespan", DEFAULT_VERIFY_AUTHN_TIMESPAN));
    }

//////////////////// Credential configuration    
    /**
     * Returns the password used for locking the keystore-file.
     *
     * Can be configured with property "Keystore.Password", defaults to
     * DEFAULT_KEYSTORE_PASSWORD.
     *
     * @return Password for the keystore-file
     */
    public String getKeystorePassword() {
        return stiamSettings.getProperty("Keystore.Password", DEFAULT_KEYSTORE_PASSWORD);
    }

    /**
     * Returns the name of the entry in the keystore used for credentials.
     *
     * Can be configured with property "Keystore.EntryName", defaults to
     * DEFAULT_KEYSTORE_ENTRY_NAME.
     *
     * @return Password for the entry in the keystore
     */
    public String getKeystoreEntryName() {
        return stiamSettings.getProperty("Keystore.EntryName", DEFAULT_KEYSTORE_ENTRY_NAME);
    }

    /**
     * Returns the password used for locking the entry in the keystore.
     *
     * Can be configured with property "Keystore.EntryPassword", defaults to
     * DEFAULT_KEYSTORE_ENTRY_PASSWORD.
     *
     * @return Password for the entry in the keystore
     */
    public String getKeystoreEntryPassword() {
        return stiamSettings.getProperty("Keystore.EntryPassword", DEFAULT_KEYSTORE_ENTRY_PASSWORD);
    }

    /**
     * Initializes a Credential by loading a certificate from the keystore.
     *
     * @param alias The name of the entry in the keystore to load
     * @return The initialized Credential object
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     */
    public Credential getCredential(String alias) throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException {

        char[] pass = getKeystorePassword().toCharArray();

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream(getKeystoreFilePath())) {
            ks.load(fis, pass);
        }

        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
                new KeyStore.PasswordProtection(getKeystoreEntryPassword().toCharArray()));

        X509Certificate certificate = (X509Certificate) pkEntry.getCertificate();
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPublicKey(certificate.getPublicKey());
        credential.setPrivateKey(pkEntry.getPrivateKey());
        return credential;
    }

    /**
     * Initializes the Credential used for signing extended attribute requests.
     * Currently, this is hardcoded and the same as the verification credential.
     *
     * @return The initialized Credential object which can be used for signing
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     */
    public Credential getSignatureCredential() throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException {
        return getCredential(getKeystoreEntryName());
    }

    /**
     * Initializes the Credential used for verifying all signatures. Currently,
     * this is hardcoded and the same as the signature credential.
     *
     * @return The initialized Credential object which can be used for signing
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     */
    public Credential getVerificationCredential() throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException {
        return getCredential(getKeystoreEntryName());
    }

//////////////////// Various
    /**
     * Defines the default eligibility policy of the users stored in the
     * directory.
     *
     * Can be configured with property "EligibilityChecker.DefaultPolicy",
     * defaults to DEFAULT_ELEGIBILITY_POLICY.
     *
     * @return Default eligibility policy
     */
    public boolean getDefaultEligibilityPolicy() {
        return stiamSettings.getProperty("EligibilityChecker.DefaultPolicy", DEFAULT_ELEGIBILITY_POLICY).equalsIgnoreCase("accept");
    }

    /**
     * Defines which implementation of Directory is returned by the factory.
     *
     * Can be configured with property "AttributeService.Directory", defaults to
     * DEFAULT_DIRECTORY.
     *
     * @return Directory-type to use
     */
    public String getDirectory() {
        return stiamSettings.getProperty("AttributeService.Directory", DEFAULT_DIRECTORY);
    }

//////////////////////////////////////// Private Methods
    /**
     * Helper for loading a specified configuration-file.
     *
     * @param configFile the properties-file to load. Sets the instance-variable
     * stiamSettings if successful or resets it to null if not
     */
    private void loadConfig(String configFile) {
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configFile))) {
            stiamSettings = new Properties();
            stiamSettings.load(stream);
            logger.info("Successfully loaded configuration from {}!", configFile);
        } catch (IOException e) {
            stiamSettings = null;
        }
    }

}
