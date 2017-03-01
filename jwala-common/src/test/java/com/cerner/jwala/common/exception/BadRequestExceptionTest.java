package com.cerner.jwala.common.exception;

import org.junit.Test;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.MessageResponseStatus;

import static org.junit.Assert.assertEquals;

public class BadRequestExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final BadRequestException badRequestException = new BadRequestException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, badRequestException.getMessageResponseStatus());
    }
}
