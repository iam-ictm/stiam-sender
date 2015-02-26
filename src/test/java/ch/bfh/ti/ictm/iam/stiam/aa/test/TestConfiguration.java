/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.test;

import ch.bfh.ti.ictm.iam.stiam.aa.util.ListProperties;
import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class serves as a helper for all testsuites which need it. It tries to
 * load special test-settings and makes them accessible.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class TestConfiguration extends ListProperties {

    /**
     * Overrides default constructor, tries to load test-configuration.
     */
    public TestConfiguration() {
        super();

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(StiamConfiguration.getInstance().getTestConfigurationFilePath()))) {
            this.load(stream);
        }
        catch (IOException e) {
            // if loading failed, we are just empty properties...
        }
    }
}
