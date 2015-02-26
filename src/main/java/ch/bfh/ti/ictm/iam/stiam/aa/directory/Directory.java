/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory;

import java.util.Map;

/**
 * This is the defined interface to a backend directory holding attribute values
 * about subjects.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public interface Directory {

    /**
     * Retrieves a given list of attributes for a given NameID from the backend.
     *
     * @param nameId The NameID to retrieve attributes for
     * @param attributeNames A list containing names of attributes to be
     * retrieved
     * @return A map containing attributes and their values
     * @throws DirectoryException or subclasses of it in case of errors in the
     * retrieval
     */
    public Map<String, String> fetchAttributes(String nameId, String[] attributeNames) throws DirectoryException;
}
