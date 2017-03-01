package com.cerner.jwala.ws.rest.v1.service.resource.impl;

import org.apache.commons.lang3.StringUtils;

/**
 * Validates a parameter or a series of parameters.
 *
 * Created by Jedd Cuison on 6/6/2016.
 */
public class ParamValidator {
    private boolean valid = true;

    private ParamValidator() {
        this.valid = true;
    }

    public static ParamValidator getNewInstance() {
        return new ParamValidator();
    }

    public boolean isValid() {
        return valid;
    }

    public ParamValidator isEmpty(final String val) {
        if (isValid()) {
            valid = StringUtils.isEmpty(val);
            return this;
        }
        return this;
    }

    public ParamValidator isNotEmpty(final String val) {
        if (isValid()) {
            valid = StringUtils.isNotEmpty(val);
            return this;
        }
        return this;
    }
}
