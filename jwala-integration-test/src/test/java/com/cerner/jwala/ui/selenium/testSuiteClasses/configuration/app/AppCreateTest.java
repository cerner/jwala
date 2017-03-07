package com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.app;

import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

/**
 * Created by Jedd Cuison on 2/28/2017
 */
public class AppCreateTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String APP_NAME = "zApp-" + CURRENT_TIME_MILLIS;

    @Test
    public void testCreateApp() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Apps");
        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();
        Thread.sleep(1000); // give a little time for the first element to get into focus as required by how
                            // "focus on the first element" was implemented here
        driver.switchTo().activeElement().sendKeys(APP_NAME);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/zApp");
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input")).click();
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
    }

}
