package com.cerner.jwala.common.exception;

import java.util.*;

public class InternalErrorException extends FaultCodeException {

    private final Map<String, List<String>> errorDetails;

    @SuppressWarnings("unchecked")
    public InternalErrorException(final MessageResponseStatus msgResponseStatus, final String msg) {
        super(msgResponseStatus, msg);
        errorDetails = Collections.EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus, final String theMessage,
                                  final Throwable theCause) {
        super(theMessageResponseStatus, theMessage, theCause);
        errorDetails = Collections.EMPTY_MAP;
    }

    public InternalErrorException(final MessageResponseStatus msgResponseStatus, final String msg, final Throwable t,
                                  final Map<String, List<String>> entityDetailsMap) {
        super(msgResponseStatus, msg, t);
        errorDetails = entityDetailsMap;
    }

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Collection<String> entityDetailsCollection) {
        super(theMessageResponseStatus, theMessage);
        errorDetails = new HashMap<>();
        for (final String key: entityDetailsCollection) {
            errorDetails.put(key, null);
        }
    }

    public Map<String, List<String>> getErrorDetails() {
        return errorDetails;
    }

}
