/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

/**
 * Base-class for Exceptions thrown in aa.directory
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class DirectoryException extends Exception {

    /**
     * Empty constructor calling super() for unspecific initialization
     */
    public DirectoryException() {
        super();
    }

    /**
     * Calls super(message) to initialize the Exception with an error message.
     *
     * @param message A message describing the cause of this exception
     */
    public DirectoryException(String message) {
        super(message);
    }

    /**
     * Initializes this exception with another Throwable as cause
     *
     * @param cause Originating Throwable causing this exception
     */
    public DirectoryException(Throwable cause) {
        super(cause);
    }

    /**
     * Accepts another Throwable as cause as well as a descriptive message and
     * calls the appropriate super-constructor
     *
     * @param message A message describing the cause of this exception
     * @param cause Originating Throwable causing this exception
     */
    public DirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
