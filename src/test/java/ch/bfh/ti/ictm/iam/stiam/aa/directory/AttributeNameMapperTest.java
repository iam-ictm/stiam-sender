/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testsuite for the AttributeNameMapper
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class AttributeNameMapperTest {
//////////////////////////////////////// Fields

    private static AttributeNameMapper attributeMapper;
    private static TestConfiguration testConfig;

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setUpClass() {
        testConfig = new TestConfiguration();
        try {
            attributeMapper = new AttributeNameMapper();
        }
        catch (IOException e) {
            attributeMapper = null;
        }
    }

    /**
     * A test for the attribute mapping
     *
     * @throws IOException if the mapper-configuration could not have been
     * loaded.
     */
    @Test
    public void AttributeMapping() throws IOException {
        if (attributeMapper == null) {
            return;
        }

        final String[] attributes = testConfig.getPropertyList("AttributeNameMapperTest.SourceAttributes");
        final String[] expectedMappedAttributes = testConfig.getPropertyList("AttributeNameMapperTest.MappedAttributes");

        final String[] mappedAttributesNames = attributeMapper.mapAttributeNames(attributes);

        for (int i = 0; i < attributes.length; i++) {
            assertEquals(expectedMappedAttributes[i], mappedAttributesNames[i]);
        }
    }
}
