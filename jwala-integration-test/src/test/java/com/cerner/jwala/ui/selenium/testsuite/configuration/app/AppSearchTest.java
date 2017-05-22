package com.cerner.jwala.ui.selenium.testsuite.configuration.app;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by RS045609 on 5/5/2017.
 */
public class AppSearchTest extends JwalaTest {
    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String APP_NAME1 = "zAppSearch1-" + CURRENT_TIME_MILLIS;
    private static final String APP_NAME2 = "zAppSearch2-" + CURRENT_TIME_MILLIS;

    private static final String searchApp1 = "Search1-" + CURRENT_TIME_MILLIS;
    private static final String searchApp2 = "Search2-" + CURRENT_TIME_MILLIS;


    @Test
    public void testSearchApp() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Apps");
        Thread.sleep(300);
        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();
        Thread.sleep(1000); // give a little time for the first element to get into focus as required by how
        // "focus on the first element" was implemented here
        driver.switchTo().activeElement().sendKeys(APP_NAME1);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/zApp1");
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input")).click();
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
        Thread.sleep(300);
        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();
        Thread.sleep(300); // give a little time for the first element to get into focus as required by how
        // "focus on the first element" was implemented here
        driver.switchTo().activeElement().sendKeys(APP_NAME2);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/zApp2");
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input")).click();
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();

        driver.findElement(By.xpath("//span[contains(text(), 'Search')]/following-sibling::input")).click();
        driver.switchTo().activeElement().sendKeys(searchApp1);
        driver.findElement(By.xpath("//button[text()='" + APP_NAME1 + "']"));
        isElementExists("//div[contains(text(),'Showing 1 to 1 of 1')]");

        driver.findElement(By.xpath("//span[contains(text(), 'Search')]/following-sibling::input")).clear();
        driver.switchTo().activeElement().sendKeys(searchApp2);
        driver.findElement(By.xpath("//button[text()='" + APP_NAME2 + "']"));
        driver.findElement(By.xpath("//span[contains(text(), 'Search')]/following-sibling::input")).clear();
        isElementExists("//div[contains(text(),'Showing 1 to 1 of 1')]");

        driver.switchTo().activeElement().sendKeys("zApp");

        waitClick(By.xpath("//tr[td[button[text()='" + APP_NAME1 + "']]]"));
        waitClick(By.xpath("//button[span[text()='Delete']]"));
        waitClick(By.xpath("//button[span[text()='Yes']]"));
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + APP_NAME1 + "']"), 0));


        waitClick(By.xpath("//tr[td[button[text()='" + APP_NAME2 + "']]]"));
        waitClick(By.xpath("//button[span[text()='Delete']]"));
        waitClick(By.xpath("//button[span[text()='Yes']]"));
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + APP_NAME2 + "']"), 0));
    }
}
