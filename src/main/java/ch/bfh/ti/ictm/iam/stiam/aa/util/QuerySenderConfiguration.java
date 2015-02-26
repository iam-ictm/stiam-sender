/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Configuration for QuerySender
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class QuerySenderConfiguration extends ListProperties {

    /**
     * Overrides default constructor, tries to load QuerySender-configuration.
     *
     * @throws java.io.IOException if loading of configuration fails
     */
    public void load() throws IOException {
        this.load(new BufferedInputStream(new FileInputStream(StiamConfiguration.getInstance().getQuerySenderFilePath())));
    }
}
