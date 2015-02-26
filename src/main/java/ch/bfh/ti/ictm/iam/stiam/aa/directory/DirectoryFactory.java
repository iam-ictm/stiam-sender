/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap.DirectoryImpl;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.property.PropertyDirectory;
import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A factory creating Directory-instances based on configuration. This is a
 * singleton.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class DirectoryFactory {
//////////////////////////////////////// Fields

    private static final Logger logger = getLogger(StiamConfiguration.class);
    private static final DirectoryFactory instance = new DirectoryFactory();
    private static Directory directoryInstance;

//////////////////////////////////////// Constructors    
    /**
     * Private constructor, initializes the configured directory.
     */
    private DirectoryFactory() {
        String directoryType = StiamConfiguration.getInstance().getDirectory();

        if (directoryType.equalsIgnoreCase("ldap")) {
            directoryInstance = new DirectoryImpl();
        } else if (directoryType.equalsIgnoreCase("property")) {
            directoryInstance = new PropertyDirectory();
        } else {
            logger.error("Unknown directory type found in configuration: {}", directoryType);
            directoryInstance = null;
        }
    }

//////////////////////////////////////// Methods
    /**
     * @return The one and only instance of this factory. (Singleton)
     */
    public static DirectoryFactory getInstance() {
        return instance;
    }

    /**
     * @return An instance of Directory, depending on the configuration.
     */
    public Directory createDirectory() {
        return directoryInstance;
    }
}
