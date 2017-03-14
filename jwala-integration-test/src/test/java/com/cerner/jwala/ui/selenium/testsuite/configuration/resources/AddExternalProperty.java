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
public class AddExternalProperty extends JwalaTest {

    @Test
    public void addExternalProperties() {
        clickTab("Configuration");
        clickTab("Resources");
        driver.findElement(By.xpath("//span[text()='Ext Properties']")).click();
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-plusthick') and @title='create']")).click();
        driver.findElement(By.xpath("//input[@type='file']"))
                .sendKeys(this.getClass().getClassLoader().getResource("selenium/test.properties").getPath()
                .replaceFirst("/", ""));
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBe(By.xpath("//li/span[text()='ext.properties']"), 1));

        // Check if the mechanism that prevents users from adding another external properties resource is working
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-plusthick') and @title='create']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.numberOfElementsToBe(
                By.xpath("//span[text()='Only one external properties file can be uploaded. Any existing ones will be overwritten.']"), 1));
        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
    }

}
