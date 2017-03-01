package com.cerner.jwala.service;

/**
 * Db server service exception wrapper
 *
 * Created by Jedd Cuison on 8/30/2016.
 */
public class DbServerServiceException extends RuntimeException {

    public DbServerServiceException(final Throwable t) {
        super(t);
    }
}
