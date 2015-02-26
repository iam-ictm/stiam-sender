/*
 * Copyright 2014 Pascal Mainini, Marc Kunz
 * Licensed under MIT license, see included file LICENSE or
 * http://opensource.org/licenses/MIT
 */
package ch.bfh.ti.ictm.iam.stiam.aa.util.saml;

/**
 * Representation of a SAML-Attribute within STIAM-AA.
 *
 * @author Pascal Mainini
 * @author Marc Kunz
 */
public class Attribute {
//////////////////////////////////////// Fields

    private String name;
    private String friendlyName;
    private String nameFormat;
    private String value;

//////////////////////////////////////// Constructors
    /**
     * Constructor which allows to set all relevant values directly.
     *
     * @param name The name of the attribute
     * @param friendlyName The friendly name of the attribute
     * @param nameFormat The format of the attribute name
     * @param value The value of the attribute (null if not defined)
     */
    public Attribute(String name, String friendlyName, String nameFormat, String value) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.nameFormat = nameFormat;
        this.value = value;
    }

    /**
     * Constructor which takes the relevant values from an OpenSAML2-Attribute.
     * Does NOT set the value!
     *
     * @param attribute An OpenSAML2-Attribute to take the values from
     */
    public Attribute(org.opensaml.saml2.core.Attribute attribute) {
        this(attribute.getName(), attribute.getFriendlyName(), attribute.getNameFormat(), null);
    }

    /**
     * Constructor which takes the relevant values out of a String[].
     *
     * @param data String[] containing { name, nameFormat, friendlyName, value }
     */
    public Attribute(String[] data) {
        this.name = data[0];
        if (data.length >= 2) {
            this.nameFormat = data[1];
        }
        if (data.length >= 3) {
            this.friendlyName = data[2];
        }
        if (data.length >= 4) {
            this.value = data[3];
        }
    }

//////////////////////////////////////// Methods
    /**
     * @return the name of the attribute
     */
    public String getName() {
        return name;
    }

    /**
     * @return the friendly name of the attribute
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @return the name format of the attribute
     */
    public String getNameFormat() {
        return nameFormat;
    }

    /**
     * @return the value of the attribute
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the attribute
     *
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return A meaningful String-representation of an attribute
     */
    @Override
    public String toString() {
        return "Name: '" + name + "', friendly name: '" + friendlyName
                + "', name format: '" + nameFormat + "', value: '" + value + "'";
    }
}
