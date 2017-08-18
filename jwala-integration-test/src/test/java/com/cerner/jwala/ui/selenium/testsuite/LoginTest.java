package com.cerner.jwala.ui.selenium.testsuite;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test login
 * Created by Jedd Cuison on 2/23/2017
 */
public class LoginTest extends JwalaTest {

    @Test
    public void testLogin() {
        driver.get(getBaseUrl() + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.pwd"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        new WebDriverWait(driver, 60).until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//li[a[text()='Operations']]")));
    }

}
