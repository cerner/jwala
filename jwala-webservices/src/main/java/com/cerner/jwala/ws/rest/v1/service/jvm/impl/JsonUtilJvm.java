package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jedd Cuison on 6/3/14.
 */
class JsonUtilJvm {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtilJvm.class);

    private JsonUtilJvm() {
    }

    /**
     * Convert a string to an Integer.
     * Return null if the string cannot be converted.
     * @param val the string value to convert
     * @return Integer. Null if conversion fails.
     */
    static Integer stringToInteger(final String val) {
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException nfe) {
            LOGGER.info("Unable to convert String to Integer", nfe);
            return null;
        }
    }

}
