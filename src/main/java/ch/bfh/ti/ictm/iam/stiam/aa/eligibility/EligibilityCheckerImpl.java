/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.eligibility;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a properties-file based implementation of an EligibilityChecker. The
 * configuration is specified in a properties-file whose location can be
 * specified in the main AA configuration.
 *
 * If it is not explicitly defined if an user is eligible or not, then it
 * returns the default value, which can also be defined in the global setting
 * 'DefaultEligibilityPolicy'
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class EligibilityCheckerImpl implements EligibilityChecker {
//////////////////////////////////////// Fields

    private final StiamConfiguration stiamSettings = StiamConfiguration.getInstance();
    private final Properties eligibilityProperties;
    private static final Logger logger = LoggerFactory.getLogger(EligibilityCheckerImpl.class);

//////////////////////////////////////// Constructors
    /**
     * Initialize the Eligibility-Checker and try to load properties-file.
     *
     * @throws IOException if the property-file is not available.
     */
    public EligibilityCheckerImpl() throws IOException {
        final String eligibilityCheckerFilePath = stiamSettings.getEligibilityCheckerFilePath();
        logger.info("Trying to load EligibilityChecker-properties from file '{}'...", eligibilityCheckerFilePath);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(eligibilityCheckerFilePath))) {
            eligibilityProperties = new Properties();
            eligibilityProperties.load(stream);
            logger.info("Sucessfully loaded EligibilityChecker-properties!");
        }
    }

//////////////////////////////////////// Public methods
    /**
     * Implementation of isEligible from EligibilityChecker. Performs the actual
     * eligibility-check.
     *
     * @param nameId NameID of the subject to check the eligibility for
     * @return true if a Subject is eligible, false if not
     */
    @Override
    public boolean isEligible(String nameId) {
        if (eligibilityProperties.containsKey(nameId) && !eligibilityProperties.getProperty(nameId).isEmpty()) {
            String policy = eligibilityProperties.getProperty(nameId);

            if (policy.equalsIgnoreCase("accept")) {
                logger.debug("Is {} eligible? true", nameId);
                return true;
            } else if (policy.equalsIgnoreCase("deny")) {
                logger.debug("Is {} eligible? false", nameId);
                return false;
            }
        }

        logger.debug("NameID '{}' not found, returning default: {}", nameId, stiamSettings.getDefaultEligibilityPolicy());
        return stiamSettings.getDefaultEligibilityPolicy();
    }
}
