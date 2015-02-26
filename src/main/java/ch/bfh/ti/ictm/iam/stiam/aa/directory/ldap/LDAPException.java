/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;

/**
 * Exception class for errors occurring in communication with an LDAP-directory.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class LDAPException extends DirectoryException {

    /**
     * Calls super(message) to initialize the Exception with an error message.
     *
     * @param message A message describing the cause of this exception
     */
    LDAPException(String message) {
        super(message);
    }

    /**
     * Accepts another Throwable as cause as well as a descriptive message and
     * calls the appropriate super-constructor
     *
     * @param message A message describing the cause of this exception
     * @param cause Originating Throwable causing this exception
     */
    public LDAPException(String message, Throwable cause) {
        super(message, cause);
    }
}
