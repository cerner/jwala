package com.cerner.jwala.ui.selenium.configuration.resources;

import com.cerner.jwala.ui.selenium.SeleniumTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import static org.junit.Assert.*;

/**
 * Created on 11/14/2016.
 */
public class UploadModifySaveDeleteExternalPropertiesTest extends SeleniumTestCase {
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        setUpSeleniumDrivers();
    }

    @Test
    public void testResourceExternalPropertiesUploadModifySaveDelete() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//div[@id='group-operations-table_wrapper']/div"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.linkText("Configuration")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.linkText("JVM")))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        waitABit();

        driver.findElement(By.linkText("Resources")).click();

        // upload an external properties file
        driver.findElement(By.xpath("//span[text()=\"Ext Properties\"]")).click();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-plusthick")).click();
        driver.findElement(By.name("templateFile")).clear();
        driver.findElement(By.name("templateFile")).sendKeys(properties.getProperty(PROPERTY_JWALA_RESOURCES_UPLOAD_DIR) + properties.getProperty(PROPERTY_JWALA_PATH_SEPARATOR) + EXTERNAL_PROPERTIES_FILE_NAME);
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.xpath("//span[text()=\"ext.properties\"]"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        // modify the external properties file
        driver.findElement(By.xpath("//span[text()=\"ext.properties\"]")).click();
        driver.findElement(By.cssSelector("li.ui-state-active > span")).click();
        for (int second = 0; ; second++) {
            if (second >= 60) fail("timeout");
            try {
                if (isElementPresent(By.cssSelector("span.ui-icon.ui-icon-disk"))) break;
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        Actions actions = new Actions(driver);
        driver.findElement(By.xpath("//pre")).click();

        // put the cursor at the beginning of the text area and create a new line before entering the text
        actions.sendKeys(Keys.HOME).sendKeys(Keys.ENTER).sendKeys(Keys.ARROW_UP).sendKeys("selenium.test.property=running selenium tests ${vars['resources.enabled']}").build().perform();
        assertEquals("selenium.test.property=running selenium tests ${vars['resources.enabled']}", driver.findElement(By.xpath("//pre")).getText());

        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-disk")).click();
        waitABit();
        driver.findElement(By.linkText("Template Preview")).click();
        waitABit();
        assertEquals("selenium.test.property=running selenium tests true", driver.findElement(By.xpath("//div[2]/div/div/div/div[6]/div/div/div/div/div[5]/div/pre")).getText());

        // upload an external properties file again and check that the warning appears
        driver.findElement(By.xpath("//span[text()=\"Ext Properties\"]")).click();
        waitABit();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-plusthick")).click();
        waitABit();
        assertEquals("Only one external properties file can be uploaded. Any existing ones will be overwritten.", driver.findElement(By.cssSelector("span.msg")).getText());
        driver.findElement(By.name("templateFile")).clear();
        driver.findElement(By.name("templateFile")).sendKeys(properties.getProperty(PROPERTY_JWALA_RESOURCES_UPLOAD_DIR) + properties.getProperty(PROPERTY_JWALA_PATH_SEPARATOR) + EXTERNAL_PROPERTIES_FILE_NAME);
        driver.findElement(By.xpath("(//span[text()=\"Ok\"])")).click();
        waitABit();
        driver.findElement(By.xpath("//span[text()=\"ext.properties\"]")).click();
        waitABit();
        assertEquals("external.property.one=1", driver.findElement(By.xpath("//pre")).getText());
        driver.findElement(By.linkText("Template Preview")).click();
        waitABit();
        assertEquals("external.property.one=1", driver.findElement(By.xpath("//div[2]/div/div/div/div[6]/div/div/div/div/div[5]/div/pre")).getText());

        // delete the file
        driver.findElement(By.cssSelector("input.noSelect")).click();
        waitABit();
        driver.findElement(By.cssSelector("span.ui-icon.ui-icon-trash")).click();
        waitABit();
        driver.findElement(By.xpath("(//button[@type='button'])[2]")).click();
        waitABit();
        assertTrue(isElementPresent(By.xpath("//span[text()=\"No resources found...\"]")));
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    @Override
    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
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
