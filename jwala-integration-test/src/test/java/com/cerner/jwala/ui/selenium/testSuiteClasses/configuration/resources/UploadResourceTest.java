package com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.resources;

import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/1/2017
 */
public class UploadResourceTest extends JwalaTest {

    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;

    @Test
    public void testUploadResource() {
        clickTab("Configuration");
        clickTab("Resources");
        clickTreeItemExpandCollapseIcon(GROUP_NAME_2);
        driver.findElement(By.xpath("//li[span[text()='" + GROUP_NAME_2 + "']]//span[text()='JVMs']")).click();
        new WebDriverWait(driver, 5).until(ExpectedConditions.numberOfElementsToBe(
                By.xpath("//li[span[text()='" + GROUP_NAME_2 + "']]//span[text()='JVMs' and contains(@class, 'ui-state-active')]"), 1));
        driver.findElement(By.xpath("//li[contains(@class, 'button')]/span[@title='create']")).click();
        driver.switchTo().activeElement().sendKeys("jwala-logo");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/jwala");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement()
                .sendKeys(this.getClass().getClassLoader().getResource("selenium/jwala-logo.png").getPath().replaceFirst("/", ""));
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();

        testForBusyIcon(3, 10);
    }

}
