/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util.saml;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import javax.xml.transform.TransformerException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;

/**
 * Testsuite for the ExtendedAttributeQueryBuilder
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class ExtendedAttributeQueryBuilderTest {

    /**
     * Basic test of the ExtendedAttributeQueryBuilder
     */
    @Test
    public void testExtendedAttributeQueryBuilder() {
        final TestConfiguration testConfig = new TestConfiguration();

        try {
            final String[] attributeProperties = testConfig.getPropertyList("ExtendedAttributeQueryBuilderTest.Attributes");
            final ArrayList<String[]> attributes = new ArrayList<>(attributeProperties.length);
            for (String attributeProperty : attributeProperties) {
                attributes.add(testConfig.getPropertyList(attributeProperty));
            }

            final ExtendedAttributeQueryBuilder builder = new ExtendedAttributeQueryBuilder(attributes);
            final String query = builder.build();
            assertNotNull(query);
            assertThat(query.length(), is(Integer.valueOf(testConfig.getProperty("ExtendedAttributeQueryBuilderTest.ExpectedLength"))));
        }
        catch (ConfigurationException ex) {
            fail("Configuration problem: " + ex.toString());
        }
        catch (NoSuchAlgorithmException ex) {
            fail("Error finding algorithm: " + ex.toString());
        }
        catch (IOException ex) {
            fail("IO problem: " + ex.toString());
        }
        catch (KeyStoreException ex) {
            fail("Error while intializing keystore: " + ex.toString());
        }
        catch (CertificateException ex) {
            fail("Error with the certificate: " + ex.toString());
        }
        catch (UnrecoverableEntryException ex) {
            fail("Unrecoverable entry: " + ex.toString());
        }
        catch (SecurityException ex) {
            fail("Security problem: " + ex.toString());
        }
        catch (MarshallingException ex) {
            fail("Issue while marshalling: " + ex.toString());
        }
        catch (SignatureException ex) {
            fail("Issue while signing: " + ex.toString());
        }
        catch (XMLParserException ex) {
            fail("Problem with the XML parser: " + ex.toString());
        }
        catch (TransformerException ex) {
            fail("Problem with the XML transformer: " + ex.toString());
        }
    }
}
