package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en_scouse.An;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Rahul Sayini on 7/11/2017.
 */
public class OperationsRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the Operations tab$")
    public void goToOperationsTab() {
        jwalaUi.clickTab("Operations");
    }

    @And("I generate all webservers")
    public void generateAllWebservers() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Generate Web Servers']]"));
    }

    @And("I generate all jvms")
    public void generateAllJvms() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Generate JVMs']]"));
    }

    @And("I expanded operations Group \"(.*)\"$")
    public void expandOperationsGroup(String group) {
        jwalaUi.click(By.xpath("//td[text()='" + group + "']/preceding-sibling::td"));
    }


    @And("I choose the row of the component with name \"(.*)\" and click button \"(.*)\"$")
    public void chooseRowandClickIcon(String componentName, String name) {
        jwalaUi.clickWhenReady((By.xpath("//tr/td[text()='" + componentName + "']/following-sibling::td[5]/div/button[@title='" + name + "']")));

    }

    @And("I choose the row of the component with name \"(.*)\" and click text \"(.*)\"$")
    public void chooseRowandClickText(String componentName, String textLink) {
        jwalaUi.clickWhenReady(By.xpath("//tr/td[text()='" + componentName + "']//following-sibling::td[5]/div/button[text()='" + textLink + "']"));
    }

    @And("I start all webservers")
    public void startAllWebServers() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Start Web Servers']]"));
    }

    @And("I stop all webservers")
    public void stopAllWebServers() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Stop Web Servers']]"));
    }

    @And("I start all jvms")
    public void startAllJvms() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Start JVMs']]"));
    }

    @And("I stop all jvms")
    public void stopAllJvms() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Stop JVMs']]"));
    }

    @And("I wait for component \"(.*)\" state \"(.*)\"$")
    public void componentState(String component, String status) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr/td[contains(text(),'" + component + "')]//following-sibling::td[6]/div/span[contains(text(),'" + status + "')]"));
    }

    @Then("I see a new page with url \"(.*)\"$")
    public void seePageWithUrl(String url) {

        Set<String> windows = jwalaUi.getWebDriver().getWindowHandles();
        String originalOne = jwalaUi.getWebDriver().getCurrentUrl();
        for (String currentWindow : windows) {
            jwalaUi.getWebDriver().switchTo().window(currentWindow);
            String current = jwalaUi.getWebDriver().getCurrentUrl();
            if (!originalOne.equals(current)) {
                assertTrue(current.contains(url));
            }
        }
    }

    @And("I wait for popup string \"(.*)\"$")
    public void waitForSomeTime(String value) throws InterruptedException {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(), '" + value + "')]"), (long)240000);
    }

    @And("I generate webapp \"(.*)\"$")
    public void generateWebapp(String appName) {
        jwalaUi.sleep();
        jwalaUi.clickWhenReady(By.xpath("//tr/td[text()='" + appName + "']/following-sibling::td[3]/div/button[@title='Generate and deploy the webapp resources.']"));
    }

    @And("I click on ok button")
    public void clickOkButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }


}