package com.cerner.jwala.ui.selenium.testsuite.operations;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 4/19/2017
 */
public class WebServerOperationsPageDelete extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String WS_NAME = "zWS-" + CURRENT_TIME_MILLIS;

    private WebDriverWait webDriverWait = new WebDriverWait(driver, 60);

    @Test
    public void testDeleteJvm() throws InterruptedException {
        clickTab("Operations");

        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//span[text()='Connecting to a web socket...']"), 0));
        final By openRowBtnLocator = By.xpath("//td[text()='" + GROUP_NAME_1 + "']/preceding-sibling::td");
        final WebElement openRowBtn = webDriverWait.until(ExpectedConditions.elementToBeClickable(openRowBtnLocator));
        openRowBtn.click();

        driver.findElement(By.xpath("//tr[td[text()='" + WS_NAME + "']]/td/div[contains(@class, 'web-server-control-panel-widget')]/button[span[contains(@class, 'ui-icon-trash')]]")).click();
        driver.findElement(By.xpath("//button[span[text()='Yes']]")).click();

        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//div[contains(text(), 'successfully deleted')]"), 1));
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//td[text()='" + WS_NAME + "']"), 0));
    }


}
