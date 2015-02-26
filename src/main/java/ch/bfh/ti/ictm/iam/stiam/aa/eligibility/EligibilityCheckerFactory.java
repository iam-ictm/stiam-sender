/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.eligibility;

import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.IOException;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A factory for creating EligibilityCheckers. Currently only builds default
 * EligibilityCheckers, could be extended in future to allow other checkers with
 * - for instance - different backends.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class EligibilityCheckerFactory {
//////////////////////////////////////// Fields    

    private static final Logger logger = getLogger(StiamConfiguration.class);
    private static final EligibilityCheckerFactory instance = new EligibilityCheckerFactory();
    private static EligibilityChecker checkerInstance;

//////////////////////////////////////// Constructors    
    /**
     * Private constructor, initializes the configured directory.
     */
    private EligibilityCheckerFactory() {
        try {
            checkerInstance = new EligibilityCheckerImpl();
        }
        catch (IOException ex) {
            logger.error("Could not initialise EligibilityChecker: {}", ex.getMessage());
            checkerInstance = null;
        }
    }

//////////////////////////////////////// Methods
    /**
     * @return The one and only instance of this factory. (Singleton)
     */
    public static EligibilityCheckerFactory getInstance() {
        return instance;
    }

    /**
     * @return An instance of Directory, depending on the configuration.
     */
    public EligibilityChecker createEligibilityChecker() {
        return checkerInstance;
    }
}
