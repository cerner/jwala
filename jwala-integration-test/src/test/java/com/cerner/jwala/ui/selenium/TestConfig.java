package com.cerner.jwala.ui.selenium;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jedd Cuison on 6/28/2017
 */
@Configuration
@ComponentScan({"com.cerner.jwala.ui.selenium.component"})
public class TestConfig {

    private static final String WEB_DRIVER_CLASS = "webdriver.class";
    private static final String ELEMENT_SEARCH_RENDER_WAIT_TIME = "element.search.render.wait.time";
    private static final String SELENIUM_PROPERTY_PATH = "selenium.property.path";
    private static final String PARAMETERS_PROPERTIES = "selenium/parameters.properties";

    @Bean(name = "seleniumTestProperties")
    public Properties getProperties() throws IOException {
        return SeleniumTestHelper.getProperties();
    }

    @Bean
    public WebDriver getDriver(@Qualifier("seleniumTestProperties") final Properties properties) {
        final WebDriver webDriver = SeleniumTestHelper.createWebDriver(System.getProperty(WEB_DRIVER_CLASS));
        webDriver.manage().timeouts().implicitlyWait(Long.parseLong(properties.getProperty(ELEMENT_SEARCH_RENDER_WAIT_TIME)),
                TimeUnit.SECONDS);
        return webDriver;
    }

    @Bean
    public WebDriverWait getWebDriverWait(final WebDriver driver) {
        return new WebDriverWait(driver, 20, 100);
    }

    @Bean(name = "parameterProperties")
    public Properties getParamaterProperties() throws IOException {
        Properties prop = new Properties();
        final String propertyPath = System.getProperty(SELENIUM_PROPERTY_PATH);
        if (StringUtils.isEmpty(propertyPath)) {
            prop.load(TestConfig.class.getClassLoader().getResourceAsStream(PARAMETERS_PROPERTIES));
        } else {
            prop.load(new FileInputStream(new File(propertyPath)));
        }
        return prop;
    }
}
