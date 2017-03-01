package com.cerner.jwala.service.exception;

/**
 * Created on 2/6/2017.
 */
public class ApplicationStartupException extends RuntimeException{
    public ApplicationStartupException(String msg) {
        super(msg);
    }
}
