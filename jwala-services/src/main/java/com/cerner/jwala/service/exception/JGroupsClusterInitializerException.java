package com.cerner.jwala.service.exception;

/**
 * {@link com.cerner.jwala.service.initializer.JGroupsClusterInitializer} exception.
 *
 * Created by Jedd Cuison on 3/15/2016.
 */
public class JGroupsClusterInitializerException extends RuntimeException {

    public JGroupsClusterInitializerException(String s, Throwable throwable) {
        super(s, throwable);
    }

}
