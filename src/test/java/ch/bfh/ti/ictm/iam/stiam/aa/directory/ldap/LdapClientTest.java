/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;
import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testsuite for the LdapClient
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class LdapClientTest {
//////////////////////////////////////// Fields

    private static LdapClient ldapClient;
    private static TestConfiguration testConfig;

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setUpClass() throws LDAPException {
        testConfig = new TestConfiguration();
        if (Boolean.parseBoolean(testConfig.getProperty("LdapClientTest.Enable", "true"))) {
            ldapClient = new LdapClient();
        } else {
            ldapClient = null;
        }
    }

    /**
     * Some cleanup after the tests...
     */
    @AfterClass
    public static void closeLdapClient() {
        if (ldapClient == null) {
            return;
        }

        ldapClient.close();
    }

    /**
     * Tests a basic search query
     */
    @Test
    public void ldapConnectionGetAttributesInLDAP() {
        if (ldapClient == null) {
            return;
        }

        final String nameId = testConfig.getProperty("LdapClientTest.NameId");
        final String[] attrs = testConfig.getPropertyList("LdapClientTest.Attributes");
        final String[] expectedResults = testConfig.getPropertyList("LdapClientTest.ExpectedResults");

        getAttributes(nameId, attrs, expectedResults);

    }

    /**
     * Tests exception if NameID is not found.
     *
     * @throws DirectoryException if there is a problem with the backend
     */
    @Test(expected = DirectoryException.class)
    public void ldapConnectionUserNotFound() throws DirectoryException {
        if (ldapClient == null) {
            throw new DirectoryException();
        }

        final String nameId = testConfig.getProperty("LdapClientTest.UserNotFound.NameId");
        final String[] attrs = {""};

        ldapClient.getAttributes(nameId, attrs);
    }

    /**
     * Tests exception if user is not found.
     */
    @Test
    public void ldapConnectionAttributeNotFound() {
        if (ldapClient == null) {
            return;
        }

        final String nameId = testConfig.getProperty("LdapClientTest.NotAvailableNameId");
        final String[] attrs = testConfig.getPropertyList("LdapClientTest.NotAnAttribute");
        final String[] expectedResults = {""};

        getAttributes(nameId, attrs, expectedResults);

    }

    /**
     * Test getting attributes when connection is closed.
     */
    @Test
    public void ldapReestablishConnectionWhileGettingAttributes() {
        if (ldapClient == null) {
            return;
        }

        ldapClient.close();

        final String nameId = testConfig.getProperty("LdapClientTest.NameId");
        final String[] attrs = testConfig.getPropertyList("LdapClientTest.Attributes");
        final String[] expectedResults = testConfig.getPropertyList("LdapClientTest.ExpectedResults");

        getAttributes(nameId, attrs, expectedResults);

    }

    /**
     * Test getting attributes when connection is closed two times.
     */
    @Test
    public void ldapCloseConnectionTwiceBeforeReastablishConnection() {
        if (ldapClient == null) {
            return;
        }

        ldapClient.close();
        ldapClient.close();

        final String nameId = testConfig.getProperty("LdapClientTest.NameId");
        final String[] attrs = testConfig.getPropertyList("LdapClientTest.Attributes");
        final String[] expectedResults = testConfig.getPropertyList("LdapClientTest.ExpectedResults");

        getAttributes(nameId, attrs, expectedResults);
    }

//////////////////////////////////////// Helpers
    /**
     * Fetches attribute-values from the LDAP-directory and compares their
     * values with expected results
     *
     * @param nameId NameID used for query
     * @param attrs Names of the attributes queried
     * @param expectedResults An array with expected values for the given names
     */
    private void getAttributes(String nameId, String[] attrs, String[] expectedResults) {
        try {
            final Object[] result = ldapClient.getAttributes(nameId, attrs);
            assertTrue(result.length > 0);
            for (int i = 0; i < attrs.length; i++) {
                assertTrue(result[i] instanceof Object);
                assertEquals(expectedResults[i], result[i].toString());
            }
        }
        catch (DirectoryException e) {
            fail("Caught unexpected UserNotFoundException!");
        }
    }
}
