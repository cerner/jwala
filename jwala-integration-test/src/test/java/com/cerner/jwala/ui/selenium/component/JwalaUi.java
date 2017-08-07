package com.cerner.jwala.ui.selenium.component;

import com.cerner.jwala.ui.selenium.steps.UploadResourceRunStepException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper component that simplifies the calling of Selenium driver commands
 * <p>
 * Created by Jedd Cuison on 6/28/2017
 */
@Component
public class JwalaUi {

    @Autowired
    private WebDriver driver;

    @Autowired
    private WebDriverWait webDriverWait;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private Properties properties;

    public void clickTab(final String tabLabel) {
        final By by = By.xpath("//li[a[text()='" + tabLabel + "']]");
        if (!driver.findElement(by).getAttribute("class").equalsIgnoreCase("current")) {
            clickWhenReady(by);
            webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("AppBusyScreen")));
        }
    }

    /**
     * Wait until element is clickable before clicking
     *
     * @param by {@link By}
     */
    public void clickWhenReady(final By by) {
        webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".AppBusyScreen")));
        webDriverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    public void expandNode(final String nodeLabel) {
        try {
            final WebElement webElement = driver.findElement(
                    By.xpath("//li[span[text()='" + nodeLabel + "']]/img[@src='public-resources/img/icons/plus.png']"));
            webElement.click();
        } catch (final NoSuchElementException e) {
            // We don't ignore the error and assume that the item is already expanded since the proper test assumption
            // is that the initial state of an item is relevant
            final String msg =
                    MessageFormat.format("Failed to expand tree item {0} since it may already be expanded!", nodeLabel);
            throw new UploadResourceRunStepException(msg, e);
        }
    }

    public void collapseNode(final String nodeLabel) {
        try {
            final WebElement webElement = driver.findElement(
                    By.xpath("//li[span[text()='" + nodeLabel + "']]/img[@src='public-resources/img/icons/minus.png']"));
            webElement.click();
        } catch (final NoSuchElementException e) {
            // We don't ignore the error and assume that the item is already collapsed since the proper test assumption
            // is that the initial state of an item is relevant
            final String msg =
                    MessageFormat.format("Failed to collapse tree item {0} since it may already be collapsed!", nodeLabel);
            throw new UploadResourceRunStepException(msg, e);
        }
    }

    public void clickNode(final String nodeLabel) {
        clickWhenReady(By.xpath("//li[span[text()='" + nodeLabel + "']]/span"));
    }

    @Deprecated
    public WebElement clickTreeItemExpandCollapseIcon(final String itemLabel) {
        final WebElement webElement =
                driver.findElement(By.xpath("//li[span[text()='" + itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    @Deprecated
    public WebElement clickTreeItemExpandCollapseIcon(final WebElement parentNode, final String itemLabel) {
        final WebElement webElement =
                parentNode.findElement(By.xpath("//li[span[text()='" + itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    public String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    public void testForBusyIcon(final int showTimeout, final int hideTimeout) {
        new FluentWait(driver).withTimeout(showTimeout, TimeUnit.SECONDS).pollingEvery(100, TimeUnit.MILLISECONDS)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 1));
        // Please take note that when "display none" is re-inserted, the whitespace in between the attribute and the value is not there anymore e.g.
        // display: none => display:none hence the xpath => contains(@style,'display:none')
        new WebDriverWait(driver, hideTimeout)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 0));
    }

    public void rightClick(final By by) {
        final Actions action = new Actions(driver).contextClick(driver.findElement(by));
        action.build().perform();
    }

    public boolean isElementExists(final By by) {
        try {
            driver.findElement(by);
        } catch (final NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public void sendKeys(final CharSequence val) {
        driver.switchTo().activeElement().sendKeys(val);
    }

    public void sendKeys(final By by, final CharSequence val) {
        driver.findElement(by).sendKeys(val);
    }

    public void click(final By by) {
        driver.findElement(by).click();
    }

    public void waitUntilElementIsVisible(final By by) {
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public boolean isCheckBoxChecked(final By by) {
        return driver.findElement(by).isSelected();
    }

    /**
     * Wait until an element is visible
     *
     * @param by {@link By}
     * @param timeout timeout in seconds
     */
    public void waitUntilElementIsVisible(final By by, final long timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.numberOfElementsToBe(by, 1));
    }

    public void waitUntilElementIsNotVisible(final By by) {
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(by, 0));
    }

    public void loadPath(final String path) {
        driver.get(getBaseUrl() + path);
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Web driver waits until an element or several elements (as indicated by the numberOfElements parameter)
     * located by the "by" parameter is/are existing
     *
     * @param by element locator
     * @param numberOfElements the number of elements to satisfy the "to be" condition
     */
    public void waitUntilNumberOfElementsToBe(final By by, final int numberOfElements) {
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(by, 1));
    }

    public void selectItem(final By by, final String itemName) {
        final Select select = new Select(driver.findElement(by));
        select.selectByVisibleText(itemName);
    }

    /**
     * This method is used to freeze a step to see what's going on and is not meant to be used in the finished test
     * steps!!!
     */
    public void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WebDriver getWebDriver() {
        return driver;
    }

    @PreDestroy
    public void destroy() {
        driver.close();
    }

    public WebElement getWebElement(By by) {
        return driver.findElement(by);
    }
}
