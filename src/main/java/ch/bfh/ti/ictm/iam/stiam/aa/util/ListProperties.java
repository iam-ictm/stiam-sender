/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util;

import java.util.Properties;

/**
 * An extension of java.util.properties with support for very limited lists in
 * property-values...
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
@SuppressWarnings("serial")
public class ListProperties extends Properties {
//////////////////////////////////////// Fields

    /**
     * Token used for separating entries in the values of a list-property
     */
    public static final String LIST_ENTRY_SEPARATOR = ",";

//////////////////////////////////////// Methods
    /**
     * Return the value of a given property as String[], containing all elements
     * of the property-value separated by LIST_ENTRY_SEPARATOR. If there is no
     * property with the name specified by key, null is returned.
     *
     * @param key The key of the property
     * @return An array containing the individual values of the property or null
     * if the property is not found.
     */
    public String[] getPropertyList(String key) {
        return getPropertyList(key, null);
    }

    /**
     * Return the value of a given property as String[], containing all elements
     * of the property-value separated by LIST_ENTRY_SEPARATOR. If there is no
     * property with the name specified by key, the given default value is
     * returned.
     *
     * @param key The key of the property
     * @param defaultValue A String[] to return if the specified property does
     * not exist
     * @return An array containing the individual values of the property or null
     * if the property is not found.
     */
    public String[] getPropertyList(String key, String[] defaultValue) {
        if (getProperty(key) == null) {
            return defaultValue;
        } else {
            return getProperty(key).split(LIST_ENTRY_SEPARATOR);
        }
    }
}
