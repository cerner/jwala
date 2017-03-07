package com.cerner.jwala.ui.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Properties;

import static org.junit.Assert.fail;

/**
 * Utility class that contains commonly used static methods
 * Created by Jedd Cuison on 2/22/2017
 */
public class SeleniumTestHelper {

    private static final String SELENIUM_PROPERTY_PATH = "selenium.property.path";
    private static final String TEST_PROPERTIES = "selenium/test.properties";

    /**
     * Crate an instance of a {@link WebDriver} to facilitate browser based testing
     * @param webDriverClass The name of the web driver class to use
     * @return {@link WebDriver}
     */
    public static WebDriver createWebDriver(final String webDriverClass) {
        final WebDriver driver;
        try {
            driver = (WebDriver) Class.forName(webDriverClass).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new SeleniumTestCaseException(e);
        }
        return driver;
    }

    /**
     * Checks whether an element is rendered by the browser or not
     * @param driver {@link WebDriver}
     * @param by {@link By}
     * @return true if the element is rendered
     */
    public static boolean isElementRendered(final WebDriver driver, final By by) {
        try {
            final int elementCount = driver.findElements(by).size();
            switch (elementCount) {
                case 0:
                case 1:
                    return elementCount == 1;
                default:
                    throw new SeleniumTestCaseException(MessageFormat.format("More than one element was found! By: {0}",
                            by.toString()));
            }
        } catch (final NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Wait for an element {@link HtmlElement} to get rendered
     * @param timeout in seconds that specifies how long the method should wait for the element to render
     * @param htmlElement The element to verify is it has been rendered
     */
    public static void waitForElementToRender(final long timeout, final HtmlElement htmlElement) {
        for (int second = 0;; second++) {
            if (second > timeout) {
                fail("timeout!");
            }

            if (htmlElement.isRendered()) {
               break;
            }

            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                throw new SeleniumTestCaseException("Failed to wait for element to render!", e);
            }
        }
    }

    /**
     * Describes an element that will be rendered by a browser
     */
    public interface HtmlElement {
        boolean isRendered();
    }

    public static Properties getProperties() throws IOException {
        final Properties properties = new Properties();
        final String propertyPath = System.getProperty(SELENIUM_PROPERTY_PATH);
        if (StringUtils.isEmpty(propertyPath)) {
            properties.load(SeleniumTestHelper.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES));
        } else {
            properties.load(new FileInputStream(new File(propertyPath)));
        }
        return properties;
    }

}
