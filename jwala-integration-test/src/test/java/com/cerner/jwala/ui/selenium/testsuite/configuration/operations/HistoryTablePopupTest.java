package com.cerner.jwala.ui.selenium.testsuite.configuration.operations;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/13/2017
 */
public class HistoryTablePopupTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;

    @Test
    public void testHistoryTablePopup() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Operations");
        driver.findElement(By.xpath("//td[text()='" + GROUP_NAME_1 + "']/preceding-sibling::td")).click();
        driver.findElement(By.xpath("//td[text()='" + GROUP_NAME_1 + "']/parent::tr/following-sibling::tr/td//span[contains(@class, 'ui-icon-newwin')]")).click();
        new WebDriverWait(driver, 10).until(
                ExpectedConditions.numberOfElementsToBe(By.xpath("//span[contains(@class, 'ui-dialog-title') and text()='Preview']"), 1));
        driver.findElement(By.xpath("//button[@title='close']")).click();
    }

}
