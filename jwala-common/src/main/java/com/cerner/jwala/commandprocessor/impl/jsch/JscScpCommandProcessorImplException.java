package com.cerner.jwala.commandprocessor.impl.jsch;

/**
 * Created on 3/13/2017.
 */
public class JscScpCommandProcessorImplException extends JschCommandProcessorImplException {
    public JscScpCommandProcessorImplException(String msg) {
        super(msg);
    }

    public JscScpCommandProcessorImplException(String msg, Throwable t) {
        super(msg, t);
    }
}
