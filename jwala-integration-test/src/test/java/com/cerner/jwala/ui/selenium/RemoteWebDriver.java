package com.cerner.jwala.ui.selenium;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * List of web drivers
 * Created by Jedd Cuison on 2/23/2017
 */
public enum RemoteWebDriver {

    CHROME(ChromeDriver.class), IE(InternetExplorerDriver.class), FIREFOX(FirefoxDriver.class);

    private final Class remoteWebDriverClass;

    RemoteWebDriver(final Class remoteWebDriverClass) {
        this.remoteWebDriverClass = remoteWebDriverClass;
    }

    public String getClassName() {
        return remoteWebDriverClass.getName();
    }

}
