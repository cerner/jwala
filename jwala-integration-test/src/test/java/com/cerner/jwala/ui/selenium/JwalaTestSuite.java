package com.cerner.jwala.ui.selenium;

import com.cerner.jwala.ui.selenium.testsuite.LoginTest;
import com.cerner.jwala.ui.selenium.testsuite.LogoutTest;
import com.cerner.jwala.ui.selenium.testsuite.PreFlightTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.app.AppCreateTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.app.AppSearchTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.app.AppDeleteTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.group.GroupCreateTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.group.GroupDeleteTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.jvm.JvmCreateTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.jvm.JvmDeleteTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.resources.*;
import com.cerner.jwala.ui.selenium.testsuite.configuration.webServer.WebServerCreateTest;
import com.cerner.jwala.ui.selenium.testsuite.configuration.webServer.WebServerDeleteTest;
import com.cerner.jwala.ui.selenium.testsuite.operations.BalancerManagerTest;
import com.cerner.jwala.ui.selenium.testsuite.operations.HistoryTablePopupTest;
import com.cerner.jwala.ui.selenium.testsuite.operations.JvmOperationsPageDeleteTest;
import com.cerner.jwala.ui.selenium.testsuite.operations.WebServerOperationsPageDelete;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A generic test suite
 *
 * Instructions on how to run this test
 *
 * Specify the following VM options:
 *
 * 1. web driver
 *
 *    Chrome:    -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver
 *    IE:        -Dwebdriver.class=org.openqa.selenium.ie.InternetExplorerDriver
 *
 * 2. web driver executable
 *
 *    Chrome:    -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe
 *    IE:        -Dwebdriver.ie.driver=C:/selenium/IEDriverServer.exe
 *
 * Created by Jedd Cuison on 2/22/2017
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({PreFlightTest.class, LoginTest.class, GroupCreateTest.class, JvmCreateTest.class, WebServerCreateTest.class,
        AppCreateTest.class, AppSearchTest.class, ResourceTopologyTest.class, ResourceTest.class, AddExternalProperty.class,
        ModifyExternalPropertyResource.class, DeleteExternalProperty.class, ResourceDeployTest.class, HistoryTablePopupTest.class,
        AppDeleteTest.class, JvmOperationsPageDeleteTest.class, WebServerOperationsPageDelete.class, WebServerCreateTest.class,
        WebServerDeleteTest.class, JvmCreateTest.class, JvmDeleteTest.class, GroupDeleteTest.class, BalancerManagerTest.class,
        LogoutTest.class})
public class JwalaTestSuite extends TestSuite {

    private static final String ELEMENT_SEARCH_RENDER_WAIT_TIME = "element.search.render.wait.time";
    public static final String WEB_DRIVER_CLASS = "webdriver.class";

    @BeforeClass
    public static void setup() throws IOException, InterruptedException {
        properties = SeleniumTestHelper.getProperties();
        driver = SeleniumTestHelper.createWebDriver(System.getProperty(WEB_DRIVER_CLASS));
        driver.manage().timeouts().implicitlyWait(Long.parseLong(properties.getProperty(ELEMENT_SEARCH_RENDER_WAIT_TIME)),
                TimeUnit.SECONDS);
    }

    @AfterClass
    public static void tearDown() {
        driver.close();
    }

}
