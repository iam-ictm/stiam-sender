/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;

/**
 * Exception thrown if the NameID could not be found in the backend directory.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class NameIDNotFoundException extends DirectoryException {

    /**
     * Calls super(message) to initialize the Exception with an error message.
     *
     * @param message A message describing the cause of this exception
     */
    public NameIDNotFoundException(String message) {
        super(message);
    }
}
