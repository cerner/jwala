package com.cerner.jwala.ui.selenium.testsuite.operations;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.assertEquals;

/**
 * Test the balancer manager status link
 *
 * Created by Jedd Cuison on 5/16/2017
 */
public class BalancerManagerTest extends JwalaTest {

    private static final String BAL_MGR_TEST = "bal-mgr-test-";
    private static final String GROUP_NAME = BAL_MGR_TEST + CURRENT_TIME_MILLIS;

    @Before
    public void init() {
        // create 3 web servers
        // 1 with http status path, 1 with https and the 1 with a relative path

        final String [] groupNameArray = {GROUP_NAME};
        createGroup(GROUP_NAME);
        createWebServer(BAL_MGR_TEST + "http-" + CURRENT_TIME_MILLIS, "localhost", groupNameArray, null,"81", "444");
        createWebServer(BAL_MGR_TEST + "https-" + CURRENT_TIME_MILLIS, "localhost", groupNameArray,
                "https://localhost:444/index.html", "81", "444");
        createWebServer(BAL_MGR_TEST + "relative-" + CURRENT_TIME_MILLIS, "localhost", groupNameArray,
                "/index.html", "22", "333");
    }

    @Test
    public void testBalancerStatus() {
        assertEquals("http://localhost:81/balancer-manager", clickBalancerStatus(BAL_MGR_TEST + "http-" + CURRENT_TIME_MILLIS));
        assertEquals("https://localhost:444/balancer-manager", clickBalancerStatus(BAL_MGR_TEST + "https-" + CURRENT_TIME_MILLIS));
        assertEquals("http://localhost:22/balancer-manager", clickBalancerStatus(BAL_MGR_TEST + "relative-" + CURRENT_TIME_MILLIS));
    }

    private String clickBalancerStatus(final String webServerName) {
        final String tocWindowHandle = driver.getWindowHandle();
        final String url;
        String balanceManagerHandle = null;

        clickTab("Configuration");
        clickTab("Operations");
        waitUntilElementIsNotVisible(By.xpath("//span[text()='Connecting to a web socket...']"));
        clickWhenReady(By.xpath("//td[text()='" + GROUP_NAME + "']/preceding-sibling::td"));
        clickWhenReady(By.xpath("//tr[td[text()='" + webServerName + "']]/td/div/button[text()='status']"));

        for (final String handle: driver.getWindowHandles()) {
            if (!handle.equalsIgnoreCase(tocWindowHandle)) {
                balanceManagerHandle = handle;
                break;
            }
        }

        driver.switchTo().window(balanceManagerHandle);
        url = driver.getCurrentUrl();
        driver.close();
        driver.switchTo().window(tocWindowHandle);
        return url;
    }

    @After
    public void destroy() {
        deleteWebServer(BAL_MGR_TEST + "http-" + CURRENT_TIME_MILLIS);
        deleteWebServer(BAL_MGR_TEST + "https-" + CURRENT_TIME_MILLIS);
        deleteWebServer(BAL_MGR_TEST + "relative-" + CURRENT_TIME_MILLIS);
        deleteGroup(GROUP_NAME);
    }

    /**
     * Create a group
     * @param name the name of the group to be created
     */
    private void createGroup(final String name) {
        clickTab("Configuration");
        clickTab("Group");
        clickWhenReady(By.xpath("//button[span[text()='Add']]"));
        sendKeys(By.xpath("//input[@name='name']"), name);
        click(By.xpath("//button[span[text()='Ok']]"));
        waitUntilElementIsVisible(By.xpath("//button[text()='" + name + "']"));
    }

    /**
     * Create a web server
     * @param webServerName the web server name
     * @param hostName the host name
     * @param groupNameArray the array that contains the group name
     * @param statusPath the status path
     * @param httpPort the http port
     * @param httpsPort the https port
     */
    private void createWebServer(final String webServerName, final String hostName, final String [] groupNameArray,
                                 final String statusPath, final String httpPort, final String httpsPort) {
        clickTab("Configuration");
        clickTab("Web Servers");
        clickWhenReady(By.xpath("//button[span[text()='Add']]"));
        sendKeys(webServerName);

        sendKeys(Keys.TAB);
        sendKeys(hostName);

        sendKeys(Keys.TAB);
        sendKeys(httpPort);

        sendKeys(Keys.TAB);
        sendKeys(httpsPort);

        sendKeys(Keys.TAB); // go to status path, if it is empty Jwala will auto generate a status path
        if (StringUtils.isNotEmpty(statusPath)) {
            driver.switchTo().activeElement().clear();
            sendKeys(statusPath);
        }

        sendKeys(Keys.TAB);
        final Select httpdDropDown = new Select(driver.switchTo().activeElement());
        httpdDropDown.selectByIndex(1);

        sendKeys(Keys.TAB);
        for (final String groupName : groupNameArray) {
            click(By.xpath("//div[contains(text(), '" + groupName + "')]/input"));
        }

        click(By.xpath("//button[span[text()='Ok']]"));

        waitUntilElementIsVisible(By.xpath("//button[text()='" + webServerName + "']"));
    }

    /**
     * Deletes a group
     * @param name the group to delete
     */
    private void deleteGroup(final String name) {
        clickTab("Configuration");
        clickTab("Group");
        clickWhenReady(By.xpath("//tr[td[button[text()='" + name + "']]]"));
        click(By.xpath("//button[span[text()='Delete']]"));
        click(By.xpath("//button[span[text()='Yes']]"));
        waitUntilElementIsNotVisible(By.xpath("//button[text()='" + name + "']"));
    }

    /**
     * Delete a web server
     * @param name the web server name
     */
    private void deleteWebServer(final String name) {
        clickTab("Configuration");
        clickTab("Web Servers");
        clickWhenReady(By.xpath("//tr[td[button[text()='" + name + "']]]"));
        click(By.xpath("//button[span[text()='Delete']]"));
        click(By.xpath("//button[span[text()='Yes']]"));
        waitUntilElementIsNotVisible(By.xpath("//button[text()='" + name + "']"));
    }

}
