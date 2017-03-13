package com.cerner.jwala.ui.selenium.testsuite;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/1/2017
 */
public class LogoutTest extends JwalaTest {

    @Test
    public void testLogout() {
        driver.findElement(By.xpath("//a[text()='Logout']")).click();
        new WebDriverWait(driver, 5).until(ExpectedConditions.numberOfElementsToBe(By.xpath("//input[@value='Log In']"), 1));
    }

}
