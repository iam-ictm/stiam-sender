/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.property;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.AttributeNameMapper;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.Directory;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;
import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimalist implementation of a directory using a properties-file as backend.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class PropertyDirectory extends Properties implements Directory {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(PropertyDirectory.class);
    private static final String filePath = StiamConfiguration.getInstance().getPropertyDirectoryFilePath();
    private AttributeNameMapper attributeNameMapper;

//////////////////////////////////////// Constructors
    /**
     * Overrides default constructor, tries to load test-configuration.
     */
    public PropertyDirectory() {
        super();
        logger.info("Initialising directory...");

        logger.info("Loading attributes from properties-file {}", filePath);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filePath))) {
            this.load(stream);
        }
        catch (IOException ex) {
            logger.error("Error while reading attributes from file: {}", ex.getMessage());
        }

        logger.info("Initialising AttributeNameMapper...");
        try {
            attributeNameMapper = new AttributeNameMapper();
        }
        catch (IOException ex) {
            logger.error("Error while initialising AttributeNameMapper: {}" + ex.getMessage());
            attributeNameMapper = null;
        }

        logger.info("Initialisation completed!");
    }

//////////////////////////////////////// Methods
    /**
     * Implementation of fetchAttributes from Directory based on our
     * property-values.
     *
     * @param nameId The NameID to retrieve attributes for
     * @param attributeNames A list containing names of attributes to be
     * retrieved
     * @return A map containing the attributes and their values
     * @throws DirectoryException or subclasses of it in case of errors in the
     * retrieval
     */
    @Override
    public Map<String, String> fetchAttributes(String nameId, String[] attributeNames) throws DirectoryException {
        logger.info("Received request for attributes...");

        String[] mappedAttributeNames;
        if (attributeNameMapper != null) {
            logger.debug("Mapping attribute names...");
            mappedAttributeNames = attributeNameMapper.mapAttributeNames(attributeNames);
        } else {
            logger.debug("AttributeNameMapper not available, NOT performing mapping!");
            mappedAttributeNames = attributeNames;
        }

        logger.debug("Fetching attributes...");
        String[] resultAttributes = new String[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++) {
            resultAttributes[i] = getProperty(nameId + "." + mappedAttributeNames[i], "");
        }

        Map<String, String> resultMap = new HashMap<>(attributeNames.length);
        for (int i = 0; i < attributeNames.length; i++) {
            resultMap.put(attributeNames[i], resultAttributes[i]);
            logger.debug("Value for attribute '{}': '{}'", attributeNames[i], resultAttributes[i]);
        }

        logger.info("Returning results!");
        return resultMap;
    }
}
