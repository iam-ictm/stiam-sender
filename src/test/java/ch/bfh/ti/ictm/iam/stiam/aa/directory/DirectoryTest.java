/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testsuite for the DirectoryImpl
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class DirectoryTest {
//////////////////////////////////////// Fields

    private static Directory directory;
    private static TestConfiguration testConfig;

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setUpClass() {
        testConfig = new TestConfiguration();
        directory = DirectoryFactory.getInstance().createDirectory();
    }

    /**
     * Tests directory service with expected input.
     */
    @Test
    public void fetchOneAttribute() {
        final String nameId = testConfig.getProperty("DirectoryTest.NameId");
        final String[] attributes = testConfig.getPropertyList("DirectoryTest.AttributeNames");
        final String[] expectedResults = testConfig.getPropertyList("DirectoryTest.AttributeValues");

        final String[] attribute = {attributes[0]};

        try {
            Map<String, String> fetchedAttributes = directory.fetchAttributes(nameId, attribute);
            assertEquals(expectedResults[0], fetchedAttributes.get(attribute[0]));
        }
        catch (DirectoryException ex) {
            fail("Caught DirectoryException while testing...");
        }
    }

    /**
     * Tests directory service with two attributes.
     */
    @Test
    public void fetchAttributesWithAttributeMapping() {
        final String nameId = testConfig.getProperty("DirectoryTest.NameId");
        final String[] attributes = testConfig.getPropertyList("DirectoryTest.AttributeNames");
        final String[] expectedResults = testConfig.getPropertyList("DirectoryTest.AttributeValues");

        try {
            Map<String, String> fetchedAttributes = directory.fetchAttributes(nameId, attributes);
            for (int i = 0; i < attributes.length; i++) {
                assertEquals(expectedResults[i], fetchedAttributes.get(attributes[i]));
            }
        }
        catch (DirectoryException ex) {
            fail("Caught DirectoryException while testing...");
        }
    }
}
