/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.authority;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import ch.bfh.ti.ictm.iam.stiam.aa.util.saml.ExtendedAttributeQueryBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import static org.hamcrest.CoreMatchers.is;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.SignatureException;

/**
 * Testsuite for the AttributeService
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class AttributeServiceTest {
//////////////////////////////////////// Fields

    private static StringWriter stringWriter;
    private static TestConfiguration testConfig;

//////////////////////////////////////// Inner classes
    /**
     * A helper-class for mocking the servlet responses
     */
    private static abstract class ServletResponseStub implements HttpServletResponse {

        private int statusCode = 0;

        @Override
        public int getStatus() {
            return statusCode;
        }

        @Override
        public void setStatus(int sc) {
            this.statusCode = sc;
        }
    }

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setUpClass() {
        stringWriter = new StringWriter();
        testConfig = new TestConfiguration();
    }

    /**
     * Cleanup after testing...
     */
    @AfterClass
    public static void tearDownClass() {
        try {
            stringWriter.close();
        } catch (IOException ex) {
        }
        stringWriter = null;
    }

    /**
     * Test doGet() with an empty request
     */
    @Test
    public void testAttributeServiceGETEmptyRequest() {
        final AttributeService as = new AttributeService();

        final HttpServletResponse res = mockResponse();
        try {
            as.init();
            as.doGet(mockEmptyRequest(), res);
        } catch (ServletException | IOException ex) {
            fail("Error while testing servlet: " + ex.toString());
        }
        assertThat(res.getStatus(), is(200));
    }

    /**
     * Test doPost() with an empty request
     */
    @Test
    public void testAttributeServicePOSTEmptyRequest() {
        final AttributeService as = new AttributeService();

        final HttpServletResponse res = mockResponse();
        try {
            as.init();
            as.doPost(mockEmptyRequest(), res);
        } catch (ServletException | IOException ex) {
            fail("Error while testing servlet: " + ex.toString());
        }
        assertThat(res.getStatus(), is(400));
    }

    /**
     * Test doGet() with a mocked request
     */
    @Test
    public void testAttributeServiceGETAttributeRequest() {
        final AttributeService as = new AttributeService();

        final HttpServletResponse res = mockResponse();
        try {
            as.init();
            as.doGet(mockAttributeRequest(), res);
        } catch (ServletException | IOException ex) {
            fail("Error while testing servlet: " + ex.toString());
        }
        assertThat(res.getStatus(), is(200));
    }

    /**
     * Test doPost() with a mocked request
     */
    @Test
    public void testAttributeServicePOSTAttributeRequest() {
        final AttributeService as = new AttributeService();

        final HttpServletResponse res = mockResponse();
        try {
            as.init();
            as.doPost(mockAttributeRequest(), res);
        } catch (ServletException | IOException ex) {
            fail("Error while testing servlet: " + ex.toString());
        }
        assertThat(res.getStatus(), is(200));
    }

//////////////////////////////////////// Helpers
    /**
     * @return a mocked-up HttpServletResponse with the ability to store status
     */
    private HttpServletResponse mockResponse() {
        final HttpServletResponse res = mock(ServletResponseStub.class);
        try {
            when(res.getWriter()).thenReturn(new PrintWriter(stringWriter, true));
        } catch (IOException ex) {
            fail("Could not initialise writer of servletResponse: " + ex.toString());
        }

        doCallRealMethod().when(res).setStatus(anyInt());
        doCallRealMethod().when(res).getStatus();

        return res;
    }

    /**
     * @return a mocked-up HttpServletRequest with method POST and destination
     * AA
     */
    private HttpServletRequest mockEmptyRequest() {
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURL()).thenReturn(new StringBuffer(testConfig.getProperty("AttributeServiceTest.RequestURL", "http://localhost:8080/")));
        return req;
    }

    /**
     * @return a mocked-up HttpServletRequest containing a full SAML extended
     * attribute query
     */
    private HttpServletRequest mockAttributeRequest() {
        final HttpServletRequest req = mockEmptyRequest();
        String[] attributeProperties = testConfig.getPropertyList("AttributeServiceTest.Attributes");
        ArrayList<String[]> attributes;
        if (attributeProperties != null) {
            attributes = new ArrayList<>(attributeProperties.length);
            for (String attributeProperty : attributeProperties) {
                attributes.add(testConfig.getPropertyList(attributeProperty));
            }
        } else {
            attributes = new ArrayList<>(0);
        }
        try {
            when(req.getParameter("SAMLRequest")).thenReturn(
                    new ExtendedAttributeQueryBuilder(attributes).buildBase64());
        } catch (ConfigurationException | NoSuchAlgorithmException | IOException |
                KeyStoreException | CertificateException | UnrecoverableEntryException |
                SecurityException | MarshallingException | SignatureException |
                TransformerException | XMLParserException ex) {
            fail("Could not initialise servlet request: " + ex.toString());
        }
        return req;
    }
}
