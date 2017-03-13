package com.cerner.jwala.ui.selenium;

import com.cerner.jwala.ui.selenium.testSuiteClasses.LoginTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.LogoutTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.PreFlightTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.app.AppCreateTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.app.AppDeleteTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.group.GroupCreateTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.group.GroupDeleteTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.jvm.JvmCreateTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.jvm.JvmDeleteTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.resources.ResourceTopologyTest;
import com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.resources.UploadResourceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A Chrome based test suite
 * Created by Jedd Cuison on 2/22/2017
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({PreFlightTest.class, LoginTest.class, GroupCreateTest.class, JvmCreateTest.class, AppCreateTest.class,
        ResourceTopologyTest.class, UploadResourceTest.class, AppDeleteTest.class, JvmDeleteTest.class, GroupDeleteTest.class,
        LogoutTest.class})
public class JwalaResourceChromeTestSuite extends TestSuite {

    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String ELEMENT_SEARCH_RENDER_WAIT_TIME = "element.search.render.wait.time";

    @BeforeClass
    public static void setup() throws IOException, InterruptedException {
        properties = SeleniumTestHelper.getProperties();
        System.setProperty(WEBDRIVER_CHROME_DRIVER, properties.getProperty(WEBDRIVER_CHROME_DRIVER));
        driver = SeleniumTestHelper.createWebDriver(RemoteWebDriver.CHROME.getClassName());
        driver.manage().timeouts().implicitlyWait(Long.parseLong(properties.getProperty(ELEMENT_SEARCH_RENDER_WAIT_TIME)),
                TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDown() {
        driver.close();
        System.clearProperty(WEBDRIVER_CHROME_DRIVER);
    }

}
