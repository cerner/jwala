package com.cerner.jwala.ui.selenium;

/**
 * {@link SeleniumTestCase} exceptions wrapper
 *
 * Created by Jedd Cuison on 2/2/2017
 */
public class SeleniumTestCaseException extends RuntimeException {

    private static final String FAILURE_TO_SETUP_SELENIUM_DRIVERS = "Failure to setup Selenium drivers!";

    public SeleniumTestCaseException(final Throwable e) {
        super(FAILURE_TO_SETUP_SELENIUM_DRIVERS, e);
    }

}
