package com.cerner.jwala.tomcat.listener.messaging.jgroups;

/**
 * Exception wrapper for {@link JGroupsStateReporter} related errors
 *
 * Created by Jedd Cuison on 8/18/2016
 */
public class JGroupsStateReporterException extends RuntimeException {

    public JGroupsStateReporterException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
