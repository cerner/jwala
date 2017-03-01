package com.cerner.jwala.common.exception;

import org.junit.Test;

import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.exception.NotFoundException;

import static org.junit.Assert.assertEquals;

public class NotFoundExceptionTest {

    @Test
    public void testGetMessageResponseStatus() {
        final MessageResponseStatus messageResponseStatus = new TestMessageResponseStatus();
        final NotFoundException notFoundException = new NotFoundException(messageResponseStatus, "message");
        assertEquals(messageResponseStatus, notFoundException.getMessageResponseStatus());
    }
}
