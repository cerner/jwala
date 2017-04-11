package com.cerner.jwala.service.exception;

/**
 * Created on 4/11/2017.
 */
public class GroupLevelAppResourceHandlerException extends RuntimeException {

    public GroupLevelAppResourceHandlerException(final String errorMessage) {
        super(errorMessage);
    }
}
