package com.cerner.jwala.common;

/**
 * Exception wrapper for {@link FileUtility} throwables
 *
 * Created by Jedd Anthony Cuison on 12/1/2016
 */
public class FileUtilityException extends RuntimeException {

    public FileUtilityException(final String msg) {
        super(msg);
    }

    public FileUtilityException(final String msg, final Throwable throwable) {
        super(msg, throwable);
    }

}
