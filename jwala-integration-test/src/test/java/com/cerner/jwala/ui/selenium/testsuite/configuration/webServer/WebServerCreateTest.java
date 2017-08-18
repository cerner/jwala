package com.cerner.jwala.ui.selenium.testsuite.configuration.webServer;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jedd Cuison on 2/28/2017
 */
public class WebServerCreateTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;

    private static final String WS_NAME = "zWS-" + CURRENT_TIME_MILLIS;

    @Test
    public void testCreateWebServer() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Servers");

        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();

        // Get the width of the dialog box so we can test if the width changed later after selecting the groups
        final WebElement jvmDlg = driver.findElement(By.xpath("//div[contains(@class, 'ui-dialog')]"));
        final int dlgWidth = jvmDlg.getSize().getWidth();

        driver.switchTo().activeElement().sendKeys(WS_NAME);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys(WS_NAME + "-host");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("80");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("443");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        assertEquals("http://" + WS_NAME + "-host:80/index.html", driver.switchTo().activeElement().getAttribute("value"));

        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        final Select httpdDropDown = new Select(driver.switchTo().activeElement());
        httpdDropDown.selectByIndex(1);

        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input")).click();
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_2 + "')]/input")).click();

        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + WS_NAME + "']"), 1));
    }

}
