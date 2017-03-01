package com.cerner.jwala.commandprocessor.impl.jsch;

/**
 * Runtime exception wrapper for {@link JschCommandProcessorImpl} related errors
 *
 * Created by JC043760 on 12/19/2016
 */
public class JschCommandProcessorImplException extends RuntimeException {

    public JschCommandProcessorImplException(final String msg) {
        super(msg);
    }

    public JschCommandProcessorImplException(final String msg, final Throwable t) {
        super(msg, t);
    }

}
