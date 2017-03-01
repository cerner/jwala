package com.cerner.jwala.service.webserver.exception;

/**
 * Created by Jedd Cuison on 5/13/2016.
 */
public class WebServerServiceException extends RuntimeException {

    public WebServerServiceException(final String message) {
        super(message);
    }

    public WebServerServiceException(final Throwable cause) {
        super(cause);
    }

    public WebServerServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
