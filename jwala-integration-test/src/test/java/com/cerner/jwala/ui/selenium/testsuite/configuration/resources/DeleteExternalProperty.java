package com.cerner.jwala.ui.selenium.testsuite.configuration.resources;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/14/2017
 */
public class DeleteExternalProperty extends JwalaTest {

    @Test
    public void delExternalProperties() {
        clickTab("Configuration");
        clickTab("Resources");
        driver.findElement(By.xpath("//li/span[text()='ext.properties']/preceding-sibling::input[@type='checkbox']")).click();
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-trash') and @title='delete']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBe(By.xpath("//b[text()='Are you sure you want to delete the property file ?']"), 1));
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBe(By.xpath("//li/span[text()='ext.properties']"), 0));
    }

}
