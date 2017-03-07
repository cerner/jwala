package com.cerner.jwala.ui.selenium.testSuiteClasses;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.fail;

/**
 * Test login
 * Created by Jedd Cuison on 2/23/2017
 */
public class LoginTest extends JwalaTest {

    @Test
    public void testLogin() throws InterruptedException {
        driver.get(getBaseUrl() + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.pwd"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            if (SeleniumTestHelper.isElementRendered(driver, By.xpath("//li[a[text()='Operations']]"))) {
                break;
            }
            Thread.sleep(1000);
        }
    }

}
