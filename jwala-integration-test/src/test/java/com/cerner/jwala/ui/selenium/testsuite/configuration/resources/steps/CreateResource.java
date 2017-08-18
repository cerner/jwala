package com.cerner.jwala.ui.selenium.testsuite.configuration.resources.steps;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Jedd Cuison on 6/13/2017
 */
public class CreateResource extends JwalaTest {

    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;
    private static final String RESOURCE_NAME_WITH_HASHTAG = "jwala-logo#2";

    @Given("^Hashtag in the resource name$")
    public void hashTagInTheResourceName() {
        clickTab("Configuration");
        clickTab("Resources");
        clickTreeItemExpandCollapseIcon(GROUP_NAME_2);
        driver.findElement(By.xpath("//li[span[text()='" + GROUP_NAME_2 + "']]//span[text()='JVMs']")).click();
        new WebDriverWait(driver, 5).until(ExpectedConditions.numberOfElementsToBe(
                By.xpath("//li[span[text()='" + GROUP_NAME_2 + "']]//span[text()='JVMs' and contains(@class, 'ui-state-active')]"), 1));
        driver.findElement(By.xpath("//li[contains(@class, 'button')]/span[@title='create']")).click();
        driver.switchTo().activeElement().sendKeys(RESOURCE_NAME_WITH_HASHTAG);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/jwala");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement()
                .sendKeys(this.getClass().getClassLoader().getResource("selenium/jwala-logo.png").getPath().replaceFirst("/", ""));
    }

    @When("^Resource is created$")
    public void resourceCreated() {
        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
    }

    @Then("^Resource creation is successful$")
    public void resourceCreatedSuccessfully() {
        waitUntilElementIsVisible(By.xpath("//span[text()='" + RESOURCE_NAME_WITH_HASHTAG +  "']"));
    }

}
