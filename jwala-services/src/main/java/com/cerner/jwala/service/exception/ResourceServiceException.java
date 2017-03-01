package com.cerner.jwala.service.exception;

/**
 * Exception wrapper from {@link com.cerner.jwala.service.resource.ResourceService}.
 *
 * Created by Jedd Cuison on 3/30/2016.
 */
public class ResourceServiceException extends RuntimeException {

    public ResourceServiceException(Throwable throwable) {
        super(throwable);
    }

    public ResourceServiceException(String s) {
        super(s);
    }

    public ResourceServiceException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }

}
