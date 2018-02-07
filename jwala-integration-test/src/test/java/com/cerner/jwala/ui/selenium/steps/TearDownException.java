package com.cerner.jwala.ui.selenium.steps;

/**
 * Created by Jedd Cuison on 8/11/2017
 */
public class TearDownException extends RuntimeException {

    public TearDownException(final String message) { super(message); }

    public TearDownException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
