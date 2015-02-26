/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.directory.ldap;

import ch.bfh.ti.ictm.iam.stiam.aa.directory.AttributeNameMapper;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.Directory;
import ch.bfh.ti.ictm.iam.stiam.aa.directory.DirectoryException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of Directory, using LdapClient as backend for
 * connecting to the user-directory and AttributeMapper for mapping attribute
 * names
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class DirectoryImpl implements Directory {
//////////////////////////////////////// Fields

    private static final Logger logger = LoggerFactory.getLogger(DirectoryImpl.class);
    private AttributeNameMapper attributeMapper;
    private LdapClient ldapClient;

//////////////////////////////////////// Constructors
    /**
     * Initialize AttributeMapper and LdapClient or set them to null if that
     * goes wrong...
     */
    public DirectoryImpl() {
        try {
            logger.info("Loading AttributeMapper...");
            attributeMapper = new AttributeNameMapper();
        }
        catch (IOException ex) {
            logger.error("Could not load attributemapper!");
            attributeMapper = null;
        }

        try {
            logger.info("Loading LdapClient...");
            ldapClient = new LdapClient();
        }
        catch (LDAPException ex) {
            logger.error("Could not load ldapclient!");
            ldapClient = null;
        }
    }

//////////////////////////////////////// Methods
    /**
     * Tries to fetch the given attributes for the given nameID by applying
     * attribute-name-mapping before.
     *
     * @param nameId NameID to search
     * @param attributeNames List of attributes to retrieve
     * @return an unmodifiable map of attributes and their values
     * @throws DirectoryException if AttributeMapper or LdapClient don't exist
     */
    @Override
    public Map<String, String> fetchAttributes(String nameId,
            String[] attributeNames) throws DirectoryException {
        logger.info("Request for attributes: {}", (Object[]) attributeNames);
        final Map<String, String> resultMap = new HashMap<>(attributeNames.length);

        if (attributeMapper == null) {
            logger.error("Could not map attributes, because AttributeMapper is not available!");
            throw new DirectoryException("AttributeMapper not available!");
        }

        logger.debug("Mapping attributes...");
        final String[] mappedAttributes = attributeMapper.mapAttributeNames(attributeNames);

        if (ldapClient == null) {
            logger.error("LDAP directory is not available!");
            throw new LDAPException("LDAP directory not available!");
        }
        final String[] resultAttributes = ldapClient.getAttributes(nameId, mappedAttributes);

        logger.debug("Build Map for attributes...");
        for (int i = 0; i < attributeNames.length; i++) {
            logger.debug("Value of '{}' is '{}'", attributeNames[i], resultAttributes[i]);
            resultMap.put(attributeNames[i], resultAttributes[i]);
        }

        logger.info("Request done, returning attribute values!");
        return Collections.unmodifiableMap(resultMap);
    }
}
