package com.cerner.jwala.service.exception;

/**
 * RuntimeException wrapper for {@link com.cerner.jwala.service.RemoteCommandExecutorService}
 *
 * Created by Jedd Cuison on 3/28/2016
 */
public class RemoteCommandExecutorServiceException extends RuntimeException {

    public RemoteCommandExecutorServiceException(final String msg) {
        super(msg);
    }

    public RemoteCommandExecutorServiceException(final Throwable throwable) {
        super(throwable);
    }

    public RemoteCommandExecutorServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }

}
