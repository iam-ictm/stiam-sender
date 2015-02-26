/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.eligibility;

import ch.bfh.ti.ictm.iam.stiam.aa.test.TestConfiguration;
import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testsuite for the EligibilityChecker
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class EligibilityCheckerTest {
//////////////////////////////////////// Fields

    private static EligibilityChecker eligibilityChecker;
    private static final TestConfiguration testConfig = new TestConfiguration();

//////////////////////////////////////// Unit-tests and initialization
    /**
     * Set up some things before running the tests...
     */
    @BeforeClass
    public static void setUpClass() {
        try {
            eligibilityChecker = new EligibilityCheckerImpl();
        }
        catch (IOException e) {
            eligibilityChecker = null;
        }
    }

    /**
     * Tests the default eligibility when a NameID does not exist
     */
    @Test
    public void defaultEligibilityCheck() {
        if (eligibilityChecker == null) {
            return;
        }
        assertFalse(eligibilityChecker.isEligible(testConfig.getProperty("EligibilityCheckerTest.NotExisting")));
    }
    
    /**
     * Tests the eligibility of an existing NameID
     */
    @Test
    public void acceptEligibilityCheck() {
        if (eligibilityChecker == null) {
            return;
        }
        assertTrue(eligibilityChecker.isEligible(testConfig.getProperty("EligibilityCheckerTest.Existing")));
    }
}
