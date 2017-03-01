package com.cerner.jwala.common.exception;

public class FaultCodeException extends RuntimeException {

    private final MessageResponseStatus messageResponseStatus;

    public FaultCodeException(final MessageResponseStatus theMessageResponseStatus,
                              final String theMessage) {
        this(theMessageResponseStatus,
             theMessage,
             null);
    }

    public FaultCodeException(final MessageResponseStatus theMessageResponseStatus,
                              final String theMessage,
                              final Throwable theCause) {
        super(theMessage,
              theCause);
        messageResponseStatus = theMessageResponseStatus;
    }

    public MessageResponseStatus getMessageResponseStatus() {
        return messageResponseStatus;
    }
}
