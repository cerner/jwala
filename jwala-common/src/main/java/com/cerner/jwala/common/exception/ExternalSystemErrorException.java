package com.cerner.jwala.common.exception;

public class ExternalSystemErrorException extends FaultCodeException {

    public ExternalSystemErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
