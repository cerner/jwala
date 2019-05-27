package com.cerner.jwala.ui.selenium;

import com.cerner.jwala.commandprocessor.jsch.impl.KeyedPooledJschChannelFactory;
import com.cerner.jwala.common.scrubber.ScrubberService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jedd Cuison on 6/28/2017
 */
@Configuration
@ComponentScan({"com.cerner.jwala.ui.selenium.component", "com.cerner.jwala.common.jsch"})
public class TestConfig {
    private static final String WEBDRIVER_PATH = "webdriver.ie.driver";
    private static final String WEB_DRIVER_CLASS = "webdriver.class";
    private static final String ELEMENT_SEARCH_RENDER_WAIT_TIME = "element.search.render.wait.time";
    private static final String TEST_PROPERTY_PATH = "test.property.path";
    private static final String PARAMETERS_PROPERTIES = "selenium/test.properties";
    private static final int SLEEP_IN_MILLIS = 100;
    private static final String WEB_DRIVER_WAIT_TIMEOUT_SEC = "web.driver.wait.timeout.sec";
    private static final String WEB_DRIVER_WAIT_TIMEOUT_SEC_DEFAULT_VALUE = "60";

    public TestConfig() throws SQLException, IOException, ClassNotFoundException {
        // make sure the db is clean before running the tests
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

    @Bean(name = "seleniumTestProperties")
    public Properties getProperties() throws IOException {
        return SeleniumTestHelper.getProperties();
    }

    @Bean
    public WebDriver getDriver(@Qualifier("seleniumTestProperties") final Properties properties) throws IOException {
        final WebDriver webDriver = SeleniumTestHelper.createWebDriver(System.getProperty(WEB_DRIVER_CLASS),System.getProperty(WEBDRIVER_PATH));
        webDriver.manage().timeouts().implicitlyWait(Long.parseLong(properties.getProperty(ELEMENT_SEARCH_RENDER_WAIT_TIME)),
                TimeUnit.SECONDS);
        return webDriver;
    }

    @Bean
    public WebDriverWait getWebDriverWait(final WebDriver driver, @Qualifier("parameterProperties") final Properties properties) {
        final long webDriverWaitTimeoutSec = Long.parseLong(properties.getProperty(WEB_DRIVER_WAIT_TIMEOUT_SEC,
                WEB_DRIVER_WAIT_TIMEOUT_SEC_DEFAULT_VALUE));
        return new WebDriverWait(driver, webDriverWaitTimeoutSec, SLEEP_IN_MILLIS);
    }

    @Bean(name = "parameterProperties")
    public Properties getParamaterProperties() throws IOException {
        Properties prop = new Properties();
        final String propertyPath = System.getProperty(TEST_PROPERTY_PATH);
        if (StringUtils.isEmpty(propertyPath)) {
            prop.load(TestConfig.class.getClassLoader().getResourceAsStream(PARAMETERS_PROPERTIES));
        } else {
            prop.load(new FileInputStream(new File(propertyPath)));
        }
        return prop;
    }

    @Bean
    public JSch getJsch(@Qualifier("seleniumTestProperties") final Properties properties) throws JSchException {
        final JSch jsch = new JSch();
        return jsch;
    }

    @Bean
    public GenericKeyedObjectPool getGenericKeyedObjectPool(final JSch jsch) throws JSchException {
        return new GenericKeyedObjectPool(new KeyedPooledJschChannelFactory(jsch));
    }

    @Bean
    public ScrubberService getScrubberService() {
        // We don't need to scrub anything in the logs since we don't do logging in our selenium tests as of the
        // moment
        return new ScrubberService() {
            @Override
            public String scrub(String raw) {
                return raw;
            }
        };
    }

    @Bean
    public Actions getActions(final WebDriver driver) {
        return new Actions(driver);
    }
}
