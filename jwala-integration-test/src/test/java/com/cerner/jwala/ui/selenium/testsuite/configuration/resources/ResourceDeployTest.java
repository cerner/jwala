package com.cerner.jwala.ui.selenium.testsuite.configuration.resources;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertFalse;

/**
 * Tests resource deployment scenarios
 *
 * Created by Jedd on 5/12/2017
 */
public class ResourceDeployTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String JVM_NAME = "zJvm-" + CURRENT_TIME_MILLIS;
    private static final String TEMPLATE_NAME = "invalid-" + CURRENT_TIME_MILLIS + ".tpl";

    @Before
    public void init() {
        clickTab("Configuration");
        clickTab("Resources");

        final WebElement groupNode = clickTreeItemExpandCollapseIcon(GROUP_NAME_1);
        clickTreeItemExpandCollapseIcon(groupNode, "JVMs");
        driver.findElement(By.xpath("//span[text()='" + JVM_NAME + "']")).click();
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-plusthick') and @title='create']")).click();
        driver.switchTo().activeElement().sendKeys(TEMPLATE_NAME);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("any");

        driver.findElement(By.xpath("//input[@type='file']"))
                .sendKeys(this.getClass().getClassLoader().getResource("selenium/invalid.tpl").getPath()
                        .replaceFirst("/", ""));

        driver.findElement(By.xpath("//span[text()='Ok']")).click();
    }

    /**
     * Deploys an invalid resource and expects an error dialog box to be shown without the details shown
     * @throws InterruptedException
     */
    @Test
    public void testDeployInvalidJvmResource() throws InterruptedException {
        rightClick(By.xpath("//span[text()='" + TEMPLATE_NAME + "']"));
        driver.findElement(By.xpath("//span[text()='deploy']")).click();
        driver.findElement(By.xpath("//span[text()='Yes']")).click();
        webDriverWait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//span[text()='ERROR']"))));

        assertFalse(isElementExists("//button[text()='Hide Details']"));

        driver.findElement(By.xpath("//span[text()='Ok']")).click();
    }

    @After
    public void destroy() {
        driver.findElement(By.xpath("//li/span[text()='" + TEMPLATE_NAME + "']/preceding-sibling::input[@type='checkbox']")).click();
        driver.findElement(By.xpath("//span[contains(@class, 'ui-icon-trash') and @title='delete']")).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .numberOfElementsToBe(
                        By.xpath("//b[text()='Are you sure you want to delete the selected resource template(s) ?']"), 1));
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
    }

}
