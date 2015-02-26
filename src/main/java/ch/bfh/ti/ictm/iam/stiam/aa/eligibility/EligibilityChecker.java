/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.eligibility;

/**
 * This is the defined interface to an eligibility checker which can check if a
 * subject identified by a NameID is eligible to use the AA or not.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public interface EligibilityChecker {

    /**
     * Performs the eligibility-check.
     *
     * @param nameId NameID of the subject to check the eligibility for
     * @return true if a Subject is eligible, false if not
     */
    public boolean isEligible(String nameId);
}
