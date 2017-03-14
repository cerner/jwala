package com.cerner.jwala.ui.selenium.testsuite.configuration.resources;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/14/2017
 */
public class ModifyExternalPropertyResource extends JwalaTest {

    private static final String TEST_STR = "The quick brown fox jumped over the lazy dog";

    @Test
    public void testModifyExtPropResource() {
        clickTab("Configuration");
        clickTab("Resources");
        driver.findElement(By.xpath("//span[text()='Ext Properties']")).click();
        driver.findElement(By.xpath("//li/span[text()='ext.properties']")).click();
        driver.findElement(By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'cm-s-default')]")).click();
        driver.switchTo().activeElement().sendKeys(TEST_STR);
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-disk') and @title='Save']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBe(By.xpath("//div[text()='Saved']"), 1));

        // Verify if it's really saved
        driver.findElement(By.xpath("//span[text()='Ext Properties']")).click();
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'cm-s-default')]"), 0));
        driver.findElement(By.xpath("//li/span[text()='ext.properties']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//div[contains(.,'" + TEST_STR + "')]"), 0));
    }

}
