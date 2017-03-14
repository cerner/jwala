package com.cerner.jwala.ui.selenium.testsuite;

import com.cerner.jwala.ui.selenium.TestSuite;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Wraps Jwala specific UI actions e.g. click tab, etc also contains methods used commonly in tests
 * Created by Jedd Cuison on 2/28/2017
 */
public class JwalaTest {

    protected final WebDriver driver = TestSuite.driver;
    protected final Properties properties = TestSuite.properties;
    protected final static long CURRENT_TIME_MILLIS = TestSuite.CURRENT_TIME_MILLIS;

    protected void clickTab(final String tabLabel) {
        final WebElement configTag = driver.findElement(By.xpath("//li[a[text()='" + tabLabel + "']]"));
        if (!configTag.getAttribute("class").equalsIgnoreCase("current")) {
            configTag.click();
        }
    }

    protected WebElement clickTreeItemExpandCollapseIcon(final String itemLabel) {
        final WebElement webElement =
                driver.findElement(By.xpath("//li[span[text()='" +  itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    protected WebElement clickTreeItemExpandCollapseIcon(final WebElement parentNode, final String itemLabel) {
        final WebElement webElement =
                parentNode.findElement(By.xpath("//li[span[text()='" +  itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    protected String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    protected void testForBusyIcon(final int showTimeout, final int hideTimeout) {
        new FluentWait(driver).withTimeout(showTimeout, TimeUnit.SECONDS).pollingEvery(100, TimeUnit.MILLISECONDS)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 1));
        // Please take note that when "display none" is re-inserted, the whitespace in between the attribute and the value is not there anymore e.g.
        // display: none => display:none hence the xpath => contains(@style,'display:none')
        new WebDriverWait(driver, hideTimeout)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 0));
    }

}
