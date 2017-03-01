package com.cerner.jwala.common.exception;

import org.junit.Test;

import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.MessageResponseStatus;

import static org.junit.Assert.assertEquals;

public class FaultCodeExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final FaultCodeException faultCodeException = new FaultCodeException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, faultCodeException.getMessageResponseStatus());
    }
}
