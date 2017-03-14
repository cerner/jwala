package com.cerner.jwala.ui.selenium;

/**
 * Selenium test exceptions wrapper
 * Created by Jedd Cuison on 2/2/2017
 */
public class SeleniumTestCaseException extends RuntimeException {

    private static final String FAILURE_TO_SETUP_SELENIUM_DRIVERS = "Failure to setup Selenium drivers!";

    public SeleniumTestCaseException(final Throwable e) {
        super(FAILURE_TO_SETUP_SELENIUM_DRIVERS, e);
    }

    public SeleniumTestCaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SeleniumTestCaseException(String message) {
        super(message);
    }

}
