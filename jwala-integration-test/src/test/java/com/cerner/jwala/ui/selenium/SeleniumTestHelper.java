package com.cerner.jwala.ui.selenium;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.ui.selenium.steps.JwalaOsType;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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

    private static final String TEST_PROPERTY_PATH = "test.property.path";
    private static final String TEST_PROPERTIES = "selenium/test.properties";
    private static final String DEFAULT_BROWSER_WIDTH = "1500";
    private static final String DEFAULT_BROWSER_HEIGHT = "1000";
    private static final String SELENIUM_GRID_HUB_URL = "selenium.grid.hub.url";
    private static final String ORG_OPENQA_SELENIUM_IE_INTERNET_EXPLORER_DRIVER = "org.openqa.selenium.ie.InternetExplorerDriver";
    private static final int SHORT_CONNECTION_TIMEOUT = 10000;
    private static final String SHELL_READ_SLEEP_DEFAULT_VALUE = "250";
    private final static Logger LOGGER = LoggerFactory.getLogger(SeleniumTestHelper.class);
    private static Properties props;
    private static JschService jschService;

    /**
     * Create an instance of a {@link WebDriver} to facilitate browser based testing
     *
     * @param webDriverClass The name of the web driver class to use
     * @return {@link WebDriver}
     */
    public static WebDriver createWebDriver(final String webDriverClass)   {
        final WebDriver driver;

        // Set the size of the browser
        final Properties properties;
        try {
            properties = SeleniumTestHelper.getProperties();
        } catch (final IOException e) {
            throw new SeleniumTestCaseException("Failure to load properties!", e);
        }

        DesiredCapabilities dc = null;

        // IE specific
        if (webDriverClass.equalsIgnoreCase(ORG_OPENQA_SELENIUM_IE_INTERNET_EXPLORER_DRIVER)) {
            dc = DesiredCapabilities.internetExplorer();
            dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
            dc.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
            dc.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
            dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        } else  {
            dc = DesiredCapabilities.chrome();
        }

        final String hubUrl;
        try {
            hubUrl = getProperties().getProperty(SELENIUM_GRID_HUB_URL);
            if (StringUtils.isNotEmpty(hubUrl)) {
                // Driver that runs tests via Selenium Grid
                driver = new RemoteWebDriver(new URL(hubUrl.replaceAll("\"", "")), dc);
            } else {
                driver = (WebDriver) Class.forName(webDriverClass).getConstructor().newInstance();
            }
        } catch (final IOException | InstantiationException | IllegalAccessException | InvocationTargetException |
                       NoSuchMethodException | ClassNotFoundException e) {
            throw new SeleniumTestCaseException("Failed to create web driver!", e);
        }

        final int width = Integer.parseInt(properties.getProperty("browser.width", DEFAULT_BROWSER_WIDTH));
        final int height = Integer.parseInt(properties.getProperty("browser.height", DEFAULT_BROWSER_HEIGHT));
        Dimension dimension = new Dimension(width, height);
        driver.manage().window().setSize(dimension);
        return driver;
    }

    public static Properties getProperties() throws IOException {
        final Properties properties = new Properties();
        final String propertyPath = System.getProperty(TEST_PROPERTY_PATH);
        if (StringUtils.isEmpty(propertyPath)) {
            properties.load(SeleniumTestHelper.class.getClassLoader().getResourceAsStream(TEST_PROPERTIES));
        } else {
            properties.load(new FileInputStream(new File(propertyPath)));
        }
        return properties;
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

    public static void checkServiceDeleteWasSuccessful(String serviceName, JschService jschSvc, Properties properties) {
        LOGGER.info("checkServiceDeleteWasSuccessful {}", serviceName);

        props = properties;
        jschService = jschSvc;

        // indirectly required by JschServiceImpl via use of ApplicationProperties
        final String originalPropertiesRootPath = System.getProperty("PROPERTIES_ROOT_PATH");
        System.setProperty("PROPERTIES_ROOT_PATH", SeleniumTestHelper.class.getResource("/selenium/vars.properties").getPath()
                .replace("/vars.properties", ""));

        final String hostname = props.getProperty("host1");

        final RemoteSystemConnection remoteSystemConnection = getRemoteSystemConnection(hostname);

        final JwalaOsType osType = getJwalaOsType(remoteSystemConnection);

        if (osType.equals(JwalaOsType.WINDOWS)) {
            checkWindowsService(serviceName, hostname, remoteSystemConnection);
        } else {
            checkLinuxServiceRunLevel(serviceName, hostname, remoteSystemConnection);
            checkLinuxService(serviceName, hostname, remoteSystemConnection);
        }

        // Restore the properties root path
        if (null != originalPropertiesRootPath) {
            System.setProperty("PROPERTIES_ROOT_PATH", originalPropertiesRootPath);
        }
    }

    private static void checkLinuxService(String serviceName, String hostname, RemoteSystemConnection remoteSystemConnection) {
        RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sudo service " + serviceName + " status", SHORT_CONNECTION_TIMEOUT);
        if (!remoteCommandReturnInfo.standardOuput.contains(serviceName + ": unrecognized service")) {
            throw new SeleniumTestCaseException(MessageFormat.format("Failed to delete the service {0} on host {1}", serviceName, hostname));
        } else {
            LOGGER.info("STD_OUT service status::{}", remoteCommandReturnInfo.standardOuput);
        }
    }

    private static void checkLinuxServiceRunLevel(String serviceName, String hostname, RemoteSystemConnection remoteSystemConnection) {
        RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sudo chkconfig --list " + serviceName, SHORT_CONNECTION_TIMEOUT);
        if (!remoteCommandReturnInfo.standardOuput.contains("error reading information on service " + serviceName + ": No such file or directory")) {
            throw new SeleniumTestCaseException(MessageFormat.format("Failed to delete {0} from runlevel on host {1}", serviceName, hostname));
        } else {
            LOGGER.info("STD_OUT chkconfig::{}", remoteCommandReturnInfo.standardOuput);
        }
    }

    private static void checkWindowsService(String serviceName, String hostname, RemoteSystemConnection remoteSystemConnection) {
        RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sc queryex " + serviceName, SHORT_CONNECTION_TIMEOUT);
        if (!remoteCommandReturnInfo.standardOuput.contains("The specified service does not exist as an installed service")) {
            throw new SeleniumTestCaseException(MessageFormat.format("Failed to delete the service {0} on host {1}", serviceName, hostname));
        } else {
            LOGGER.info("STD_OUT sc queryex::{}", remoteCommandReturnInfo.standardOuput);
        }
    }

    private static JwalaOsType getJwalaOsType(RemoteSystemConnection remoteSystemConnection) {
        RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "uname", SHORT_CONNECTION_TIMEOUT);
        LOGGER.info("uname: {}", remoteCommandReturnInfo);
        return StringUtils.indexOf(remoteCommandReturnInfo.standardOuput, "CYGWIN") > -1 ? JwalaOsType.WINDOWS : JwalaOsType.UNIX;
    }

    private static RemoteSystemConnection getRemoteSystemConnection(String hostname) {
        final String sshUser = props.getProperty("ssh.user.name");
        final String sshPwd = props.getProperty("ssh.user.pwd");

        LOGGER.info("sshUser {} :: host1: {}", sshUser, hostname);

        return new RemoteSystemConnection(sshUser, sshPwd, hostname, 22);
    }


}