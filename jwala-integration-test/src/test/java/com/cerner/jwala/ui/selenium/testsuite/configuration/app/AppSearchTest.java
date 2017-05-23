package com.cerner.jwala.ui.selenium.testsuite.configuration.app;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static org.junit.Assert.assertTrue;

/**
 * Created by Rahul Sayini on 5/5/2017
 */
public class AppSearchTest extends JwalaTest {
    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;

    private static final String APP_NAME_1 = "zAppSearch1-" + CURRENT_TIME_MILLIS;
    private static final String APP_NAME_2 = "zAppSearch2-" + CURRENT_TIME_MILLIS;

    private static final String SEARCH_APP_1 = "Search1-" + CURRENT_TIME_MILLIS;
    private static final String SEARCH_APP_2 = "Search2-" + CURRENT_TIME_MILLIS;

    @Before
    public void init() throws InterruptedException {
        clickTab("Configuration");
        clickTab("Web Apps");

        click(By.xpath("//button[span[text()='Add']]"));
        Thread.sleep(300); // give a little time for the first element to get into focus as required by how
        sendKeys(APP_NAME_1);
        sendKeys(Keys.TAB);
        sendKeys("/zApp1");
        click(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input"));
        click(By.xpath("//button[span[text()='Ok']]"));
        waitUntilElementIsVisible(By.xpath("//button[text()='" + APP_NAME_1 + "']"));

        click(By.xpath("//button[span[text()='Add']]"));
        Thread.sleep(300); // give a little time for the first element to get into focus as required by how
        sendKeys(APP_NAME_2);
        sendKeys(Keys.TAB);
        sendKeys("/zApp2");
        click(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input"));
        click(By.xpath("//button[span[text()='Ok']]"));
        waitUntilElementIsVisible(By.xpath("//button[text()='" + APP_NAME_2 + "']"));
    }

    @After
    public void destroy() {
        // refresh to make sure all rows are visible again
        // executing clear for the search box will not show
        // the rows
        driver.navigate().refresh(); // refresh to make sure all rows are visible again

        clickWhenReady(By.xpath("//tr[td[button[text()='" + APP_NAME_1 + "']]]"));
        click(By.xpath("//button[span[text()='Delete']]"));
        click(By.xpath("//button[span[text()='Yes']]"));
        waitUntilElementIsNotVisible(By.xpath("//button[text()='" + APP_NAME_1 + "']"));

        click(By.xpath("//tr[td[button[text()='" + APP_NAME_2 + "']]]"));
        click(By.xpath("//button[span[text()='Delete']]"));
        click(By.xpath("//button[span[text()='Yes']]"));
        waitUntilElementIsNotVisible(By.xpath("//button[text()='" + APP_NAME_2 + "']"));
    }

    @Test
    public void testSearchApp() {
        click(By.xpath("//span[contains(text(), 'Search')]/following-sibling::input"));
        sendKeys(SEARCH_APP_1);
        assertTrue(isElementExists("//button[text()='" + APP_NAME_1 + "']"));
        assertTrue(isElementExists("//div[contains(text(),'Showing 1 to 1 of 1')]"));

        // refresh to make sure all rows are visible again
        // executing clear for the search box will not show
        // the rows
        driver.navigate().refresh();

        click(By.xpath("//span[contains(text(), 'Search')]/following-sibling::input"));
        sendKeys(SEARCH_APP_2);
        assertTrue(isElementExists("//button[text()='" + APP_NAME_2 + "']"));
        assertTrue(isElementExists("//div[contains(text(),'Showing 1 to 1 of 1')]"));
    }

}
