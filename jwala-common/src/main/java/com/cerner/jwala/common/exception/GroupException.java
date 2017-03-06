package com.cerner.jwala.common.exception;

/**
 * Created on 3/3/2017.
 */
public class GroupException extends RuntimeException {
    public GroupException() {
        super();
    }

    public GroupException(final String message) {
        super(message);
    }

    public GroupException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public GroupException(final Throwable cause) {
        super(cause);
    }
}
