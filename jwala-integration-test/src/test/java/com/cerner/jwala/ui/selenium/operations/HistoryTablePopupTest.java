package com.cerner.jwala.ui.selenium.operations;

import com.cerner.jwala.ui.selenium.SeleniumTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class HistoryTablePopupTest extends SeleniumTestCase {
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        setUpSeleniumDrivers();
    }

    @Test
    public void testHistoryTablePopup() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div"))) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.id("group-operations-table_1")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.xpath("//div[@id='ext-comp-div-group-operations-table_1']/div/div/div"))) break;
            Thread.sleep(1000);
        }

        waitABit();

        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-newwin")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center"))) break;
            Thread.sleep(1000);
        }

        try {
            assertTrue(isElementPresent(By.cssSelector("span.ui-dialog-title.text-align-center")));
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }

        waitABit();

        driver.findElement(By.xpath("(//button[@type='button'])[5]")).click();

        waitABit();

        driver.findElement(By.linkText("Logout")).click();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }
}
