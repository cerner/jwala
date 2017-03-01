package com.cerner.jwala.ws.rest.v1.service.resource.impl;

import org.junit.Test;

import com.cerner.jwala.ws.rest.v1.service.resource.impl.ParamValidator;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ParamValidator}
 *
 * Created by Jedd Cuison on 6/6/2016.
 */
public class ParamValidatorTest {

    @Test
    public void testParamValidator() {
        assertTrue(ParamValidator.getNewInstance().isEmpty("").isEmpty("").isNotEmpty("hey").isValid());
        assertFalse(ParamValidator.getNewInstance().isEmpty("").isEmpty("").isNotEmpty("").isValid());
        assertTrue(ParamValidator.getNewInstance().isEmpty("").isEmpty(null).isNotEmpty("hello").isValid());
        assertTrue(ParamValidator.getNewInstance().isEmpty("").isEmpty(null).isNotEmpty("hello").isNotEmpty("hoho")
                .isEmpty(null).isValid());
    }
}