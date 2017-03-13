package com.cerner.jwala.ui.selenium.testsuite.configuration.group;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test create group
 * Created by Jedd Cuison on 2/27/2017
 */
public class GroupCreateTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;

    @Test
    public void testCreateGroups() {
        addGroup(GROUP_NAME_1);
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + GROUP_NAME_1 + "']"), 1));
        addGroup(GROUP_NAME_2);
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + GROUP_NAME_2 + "']"), 1));
    }

    /**
     * Add a group
     * @param name the group to delete
     */
    public void addGroup(final String name) {
        clickTab("Configuration");
        clickTab("Group");
        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();
        driver.findElement(By.xpath("//input[@name='name']")).sendKeys(name);
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
    }

}
