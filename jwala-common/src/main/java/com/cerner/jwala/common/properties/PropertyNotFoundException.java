package com.cerner.jwala.common.properties;

/**
 * Created by sg038069 on 12/23/16.
 */
public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }
}
