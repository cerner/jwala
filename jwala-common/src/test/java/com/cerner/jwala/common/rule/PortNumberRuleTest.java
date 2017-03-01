package com.cerner.jwala.common.rule;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import static org.junit.Assert.*;

public class PortNumberRuleTest {
    FaultType error = FaultType.INVALID_WEBSERVER_PORT;
    PortNumberRule pnrValid = new PortNumberRule(Integer.valueOf(1), error);
    PortNumberRule pnrNull = new PortNumberRule(null, error);
    PortNumberRule pnrOne = new PortNumberRule(Integer.valueOf(0), error);
    PortNumberRule pnrTwo = new PortNumberRule(Integer.valueOf(65536), error);

    PortNumberRule nullValueValid = new PortNumberRule(null, error, true);
    PortNumberRule nullValueInvalid1 = new PortNumberRule(null, error);
    PortNumberRule nullValueInvalid2 = new PortNumberRule(null, error, false);


    @Test
    public void testIsValid() {
        assertTrue(pnrValid.isValid());
        assertFalse(pnrNull.isValid());
        assertFalse(pnrOne.isValid());
        assertFalse(pnrTwo.isValid());
    }

    @Test
    public void testValidate() {
        pnrValid.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testValidateNotValid() {
        pnrNull.validate();
    }

    @Test
    public void testGetMessageResponseStatus() {
        assertEquals("InvalidWebServerPortNumber", pnrOne.getMessageResponseStatus().getMessage());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Port specified is invalid.", pnrNull.getMessage());
        assertEquals("Port specified is invalid (0).", pnrOne.getMessage());
    }

    @Test
    public void testPortValidationIfNullableIsTrue() {
        assertTrue(nullValueValid.isValid());
        assertEquals("Port specified is invalid.", nullValueInvalid1.getMessage());
        assertEquals("Port specified is invalid.", nullValueInvalid2.getMessage());
    }

    @Test
    public void testInvalidPortNumberWhileNullable() {
        final PortNumberRule rule = new PortNumberRule(-12,
                                                       FaultType.INVALID_WEBSERVER_PORT,
                                                       true);
        assertFalse(rule.isValid());
    }
}
