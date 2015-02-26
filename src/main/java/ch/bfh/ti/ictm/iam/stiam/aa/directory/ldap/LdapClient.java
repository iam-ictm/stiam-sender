/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;
import ch.bfh.ti.ictm.iam.stiam.aa.util.StiamConfiguration;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the connection with the LDAP-backend-directory and allows to query
 * it.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
class LdapClient {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(LdapClient.class);
    private final StiamConfiguration stiamSettings;
    private LDAPConnection ldapConnection;

//////////////////////////////////////// Constructors
    /**
     * Initialize the LdapClient and try to open the connection.
     *
     * @throws LDAPException If something fails...
     */
    LdapClient() throws LDAPException {
        logger.info("Initializing LDAP-Client...");
        stiamSettings = StiamConfiguration.getInstance();
        connect();
        logger.info("Initialization done!");
    }

//////////////////////////////////////// Methods
    /**
     * Returns requested attributes for a specified NameID
     *
     * @param nameId NameID to query for
     * @param attr An array containing attribute names requested
     * @return String-array with attribute-values in the same order as the names
     * in attr
     * @throws NameIDNotFoundException if the NameID could not have been found
     * in the directory
     */
    public String[] getAttributes(String nameId, String[] attr) throws DirectoryException {
        if (attr.length == 0) {
            logger.info("Received empty query for user '{}', returning nothing.", nameId);
            return new String[0];
        }

        if (ldapConnection == null || !ldapConnection.isConnected()) {
            logger.info("LDAP-Connection down, trying to reconnect...");
            connect();
            if (ldapConnection == null || !ldapConnection.isConnected()) {
                logger.error("Could not reconnect - cannot search attributes!");
                return new String[0];
            }
        }

        logger.info("Fetching attributes from LDAP for user '{}'...", nameId);

        final String[] resultArray = new String[attr.length];
        try {
            final SearchResult searchResults = ldapConnection.search(stiamSettings.getLdapBaseDN(),
                    SearchScope.SUB, stiamSettings.getLdapFilter(nameId), attr);
            final SearchResultEntry entry;
            if (!searchResults.getSearchEntries().isEmpty()) {
                logger.debug("Found entry for user '{}'", nameId);
                entry = searchResults.getSearchEntries().get(0);
            } else {
                logger.debug("User '{}' not found!", nameId);
                throw new NameIDNotFoundException("User '" + nameId + "' not found!");
            }

            for (int i = 0; i < attr.length; i++) {
                if (entry.hasAttribute(attr[i])) {
                    resultArray[i] = entry.getAttribute(attr[i]).getValue();
                } else {
                    resultArray[i] = "";
                }
                logger.debug("Value for attribute '{}': {}", attr[i], resultArray[i]);
            }
        }
        catch (LDAPSearchException e) {
            logger.error("Couldn't perform an LDAP-Search query! Error was: '{}'", e.getMessage());
            throw new LDAPException(e.getMessage(), e);
        }

        logger.info("Done!");
        return resultArray;
    }

    /**
     * Closes the connection to the LDAP-directory
     */
    public void close() {
        ldapConnection.close();
        logger.debug("LDAP-directory: connection closed");
    }

    /**
     * Opens the connection to the LDAP-directory
     *
     * @throws LDAPException if something goes wrong
     */
    public void connect() throws LDAPException {
        try {
            logger.debug("Trying to connect to LDAP-directory");
            ldapConnection = new LDAPConnection(stiamSettings.getLdapHost(), stiamSettings.getLdapPort(),
                    stiamSettings.getLdapUser(), stiamSettings.getLdapPassphrase());
            logger.debug("LDAP-directory: connection established");
        }
        catch (com.unboundid.ldap.sdk.LDAPException ex) {
            logger.error("LDAP-directory: connection failed");
            throw new LDAPException(ex.getMessage(), ex);
        }
    }
}
