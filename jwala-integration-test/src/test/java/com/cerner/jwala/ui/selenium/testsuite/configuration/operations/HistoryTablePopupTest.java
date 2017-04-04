package com.cerner.jwala.ui.selenium.testsuite.configuration.operations;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 3/13/2017
 */
public class HistoryTablePopupTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;

    private WebDriverWait webDriverWait = new WebDriverWait(driver, 60);

    @Test
    public void testHistoryTablePopup() {
        clickTab("Configuration");
        clickTab("Operations");
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//span[text()='Connecting to a web socket...']"), 0));
        final By openRowBtnLocator = By.xpath("//td[text()='" + GROUP_NAME_1 + "']/preceding-sibling::td");
        final WebElement openRowBtn = webDriverWait.until(ExpectedConditions.elementToBeClickable(openRowBtnLocator));
        openRowBtn.click();
        final By historyPopupBtnLocator =
                By.xpath("//td[text()='" + GROUP_NAME_1 + "']/parent::tr/following-sibling::tr/td//span[contains(@class, 'ui-icon-newwin')]");
        final WebElement historyPopupBtn = webDriverWait.until(ExpectedConditions.elementToBeClickable(historyPopupBtnLocator));
        historyPopupBtn.click();
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//span[contains(@class, 'ui-dialog-title') and text()='Preview']"), 1));
        driver.findElement(By.xpath("//button[@title='close']")).click();
    }

}
