package com.cerner.jwala.ui.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Utility class that contains commonly used static methods
 * Created by Jedd Cuison on 2/22/2017
 */
public class SeleniumTestHelper {

    private static final String SELENIUM_PROPERTY_PATH = "selenium.property.path";
    private static final String TEST_PROPERTIES = "selenium/test.properties";
    private static final String PARAMETERS_PROPERTIES = "selenium/parameters.properties";
    private static Properties paramatersProperties;

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

    public static Properties getParamatersProperties() throws IOException {
        if (paramatersProperties == null) {
            paramatersProperties = new Properties();
            final String propertyPath = System.getProperty(SELENIUM_PROPERTY_PATH);
            if (StringUtils.isEmpty(propertyPath)) {
                paramatersProperties.load(SeleniumTestHelper.class.getClassLoader().getResourceAsStream(PARAMETERS_PROPERTIES));
            } else {
                paramatersProperties.load(new FileInputStream(new File(propertyPath)));
            }
        }
        return paramatersProperties;
    }

    public static void runSqlScript(final String sqlScript) throws IOException, ClassNotFoundException, SQLException {
        final Properties properties = SeleniumTestHelper.getProperties();
        Class.forName(properties.getProperty("jwala.db.driver"));
        final String connectionStr = properties.getProperty("jwala.db.connection");
        final String userName = properties.getProperty("jwala.db.userName");
        final String password = properties.getProperty("jwala.db.password");
        try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password);
             final Statement stmt = conn.createStatement();
             final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(sqlScript)))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stmt.execute(line);
            }
        }
    }
}
