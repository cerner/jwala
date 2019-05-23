package com.cerner.jwala.ui.selenium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Utility class that contains commonly used static methods Created by Jedd
 * Cuison on 2/22/2017
 */
public class SeleniumTestHelper {
	private static final String TEST_PROPERTY_PATH = "test.property.path";
	private static final String TEST_PROPERTIES = "selenium/test.properties";
	private static final String ORG_OPENQA_SELENIUM_IE_INTERNET_EXPLORER_DRIVER = "org.openqa.selenium.ie.InternetExplorerDriver";

	/**
	 * Create an instance of a {@link WebDriver} to facilitate browser based testing
	 *
	 * @param webDriverClass
	 *            The name of the web driver class to use
	 * @return {@link WebDriver}
	 */
	public static WebDriver createWebDriver(final String webDriverClass, String webDriverPath) {
		WebDriver driver = null;
		DesiredCapabilities dc = null;
		// IE specific
		if (webDriverClass.equalsIgnoreCase(ORG_OPENQA_SELENIUM_IE_INTERNET_EXPLORER_DRIVER)) {
			System.setProperty("webdriver.ie.driver", webDriverPath);
			dc = DesiredCapabilities.internetExplorer();
			dc.setCapability(InternetExplorerDriver.ENABLE_ELEMENT_CACHE_CLEANUP, true);
			dc.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
			dc.setCapability("unexpectedAlertBehaviour", "accept");
			dc.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			dc.setCapability("disable-popup-blocking", true);
			dc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			dc.setCapability("silent", true);
			dc.setCapability("allow-blocked-content", true);
			dc.setCapability("allowBlockedContent", true);
			dc.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
			dc.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
			driver = new InternetExplorerDriver(dc);
		}
		else {
	        dc = DesiredCapabilities.chrome();
	        driver = new ChromeDriver(dc);
		}
		driver.manage().window().maximize();
		return driver;
	}
	
	/**
	 * Checks whether an element is rendered by the browser or not
	 *
	 * @param driver
	 *            {@link WebDriver}
	 * @param by
	 *            {@link By}
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
				throw new SeleniumTestCaseException(MessageFormat
						.format("More than one element was found! By: {0}", by.toString()));
			}
		} catch (final NoSuchElementException e) {
			return false;
		}
	}

	public static Properties getProperties() throws IOException {
		final Properties properties = new Properties();
		final String propertyPath = System.getProperty(TEST_PROPERTY_PATH);
		if (StringUtils.isEmpty(propertyPath)) {
			properties.load(
					SeleniumTestHelper.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES));
		} else {
			properties.load(new FileInputStream(new File(propertyPath)));
		}
		return properties;
	}

	public static void runSqlScript(final String sqlScript)
			throws IOException, ClassNotFoundException, SQLException {
		final Properties properties = SeleniumTestHelper.getProperties();
		Class.forName(properties.getProperty("jwala.db.driver"));
		final String connectionStr = properties.getProperty("jwala.db.connection");
		final String userName = properties.getProperty("jwala.db.userName");
		final String password = properties.getProperty("jwala.db.password");
		try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password);
				final Statement stmt = conn.createStatement();
				final BufferedReader bufferedReader = new BufferedReader(
						new FileReader(new File(sqlScript)))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stmt.execute(line);
			}
		}
	}
}