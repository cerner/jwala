package com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.jvm;

import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/1/2017
 */
public class JvmDeleteTest extends JwalaTest {

    private static final String JVM_NAME = "zJvm-" + CURRENT_TIME_MILLIS;

    @Test
    public void testDeleteJvm() throws InterruptedException {
        clickTab("Configuration");
        clickTab("JVM");
        driver.findElement(By.xpath("//tr[td[button[text()='" + JVM_NAME + "']]]")).click();
        driver.findElement(By.xpath("//button[span[text()='Delete']]")).click();
        driver.findElement(By.xpath("//button[span[text()='Yes']]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + JVM_NAME + "']"), 0));
    }

}
