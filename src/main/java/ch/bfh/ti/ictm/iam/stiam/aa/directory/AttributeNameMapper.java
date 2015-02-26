/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps attribute names given by the STIAM-Hub with those in the
 * directory. Can be configured by a properties-file specified in the main
 * configuration. Any argument names not mapped are given without modification
 * to the directory.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class AttributeNameMapper {
//////////////////////////////////////// Fields

    private final Properties attributeProperties;
    private static final Logger logger = LoggerFactory.getLogger(AttributeNameMapper.class);

//////////////////////////////////////// Constructors
    /**
     * Initialize the AttributeNameMapper and try to load the properties-file.
     *
     * @throws IOException in case of error while loading
     */
    public AttributeNameMapper() throws IOException {
        final String attributeMapperFilePath = StiamConfiguration.getInstance().getAttributeNameMapperFilePath();

        logger.info("Trying to load AttributeNameMapper-properties from file '{}'...", attributeMapperFilePath);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(attributeMapperFilePath))) {
            attributeProperties = new Properties();
            attributeProperties.load(stream);
            logger.info("Sucessfully loaded AttributeNameMapper-properties!", attributeMapperFilePath);
        }
    }

//////////////////////////////////////// Methods
    /**
     * Returns the mapped attribute names.
     *
     * @param attributes Source attribute names as a String array
     * @return An array containing the mapped or original attribute names
     */
    public String[] mapAttributeNames(String[] attributes) {
        logger.debug("Get mapped attributenames...");
        if (attributes.length == 0) {
            return null;
        }

        final String[] resultArray = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            String currentAttribute = attributeProperties.getProperty(attributes[i]);
            if (currentAttribute == null || currentAttribute.isEmpty()) {
                currentAttribute = attributes[i];
            }
            logger.debug("Map source AttributeName '{}' with destination AttributeName '{}'", attributes[i], currentAttribute);
            resultArray[i] = currentAttribute;
        }
        return resultArray;
    }
}
