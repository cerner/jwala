package com.cerner.jwala.common.exception;

public class BadRequestException extends FaultCodeException {

    public BadRequestException(final MessageResponseStatus theMessageResponseStatus,
                               final String theMessage) {
        this(theMessageResponseStatus,
             theMessage,
             null);
    }

    public BadRequestException(final MessageResponseStatus theMessageResponseStatus,
                               final String theMessage,
                               final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
