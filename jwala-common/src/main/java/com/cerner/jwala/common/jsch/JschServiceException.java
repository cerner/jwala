package com.cerner.jwala.common.jsch;

/**
 * Wrapper for exceptions thrown by {@link JschService}
 *
 * Created by JC043760 on 12/26/2016
 */
public class JschServiceException extends RuntimeException {

    public JschServiceException(final String msg) {
        super(msg);
    }

    public JschServiceException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
