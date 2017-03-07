package com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.app;

import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/1/2017
 */
public class AppDeleteTest extends JwalaTest {

    private static final String APP_NAME = "zApp-" + CURRENT_TIME_MILLIS;

    @Test
    public void testDeleteApp() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Apps");
        driver.findElement(By.xpath("//tr[td[button[text()='" + APP_NAME + "']]]")).click();
        driver.findElement(By.xpath("//button[span[text()='Delete']]")).click();
        driver.findElement(By.xpath("//button[span[text()='Yes']]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + APP_NAME + "']"), 0));
    }

}
