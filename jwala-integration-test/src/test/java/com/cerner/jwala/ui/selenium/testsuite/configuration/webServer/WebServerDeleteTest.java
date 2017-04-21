package com.cerner.jwala.ui.selenium.testsuite.configuration.webServer;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/1/2017
 */
public class WebServerDeleteTest extends JwalaTest {

    private static final String WS_NAME = "zWS-" + CURRENT_TIME_MILLIS;

    @Test
    public void testDeleteJvm() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Servers");
        driver.findElement(By.xpath("//tr[td[button[text()='" + WS_NAME + "']]]")).click();
        driver.findElement(By.xpath("//button[span[text()='Delete']]")).click();
        driver.findElement(By.xpath("//button[span[text()='Yes']]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + WS_NAME + "']"), 0));
    }

}
