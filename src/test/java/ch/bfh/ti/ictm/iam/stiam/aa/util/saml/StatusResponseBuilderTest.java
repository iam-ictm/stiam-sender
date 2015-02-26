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
 * Testsuite for the StatusResponseBuilder
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class StatusResponseBuilderTest {

    /**
     * Basic test of the StatusResponseBuilder
     */
    @Test
    public void testStatusResponseBuilder() {
        final TestConfiguration testConfig = new TestConfiguration();

        try {
            final ResponseBuilder builder = new ResponseBuilder(
                    testConfig.getProperty("StatusResponseBuilderTest.Destination"),
                    testConfig.getProperty("StatusResponseBuilderTest.InResponseTo"),
                    testConfig.getPropertyList("StatusResponseBuilderTest.StatusCodes"));
            final String response = builder.build();
            assertNotNull(response);
            assertThat(response.length(), is(Integer.valueOf(testConfig.getProperty("StatusResponseBuilderTest.ExpectedLength"))));
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
