package com.cerner.jwala.ui.selenium.testsuite.configuration.group;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test delete group
 * Created by Jedd Cuison on 2/27/2017
 */
public class GroupDeleteTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;

    @Test
    public void testDeleteGroups() {
        deleteGroup(GROUP_NAME_1);
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + GROUP_NAME_1 + "']"), 0));
        deleteGroup(GROUP_NAME_2);
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + GROUP_NAME_2 + "']"), 0));
    }

    /**
     * Deletes a group
     * @param name the group to delete
     */
    private void deleteGroup(final String name) {
        clickTab("Configuration");
        clickTab("Group");
        driver.findElement(By.xpath("//tr[td[button[text()='" + name + "']]]")).click();
        driver.findElement(By.xpath("//button[span[text()='Delete']]")).click();
        driver.findElement(By.xpath("//button[span[text()='Yes']]")).click();
    }

}
