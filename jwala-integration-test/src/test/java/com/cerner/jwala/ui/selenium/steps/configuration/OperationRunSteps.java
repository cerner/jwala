package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Rahul Sayini on 7/11/2017.
 */
public class OperationRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the Operations tab$")
    public void goToOperationsTab() {
        jwalaUi.clickTab("Operations");
    }

    @And("I generate all webservers")
    public void generateAllWebservers() {
        jwalaUi.click(By.xpath("//button[span[text()='Generate Web Servers']]"));
    }

    @And("I generate all jvms")
    public void generateAllJvms() {
        jwalaUi.click(By.xpath("//button[span[text()='Generate JVMs']]"));
    }

    @And("I start all webservers")
    public void startAllWebServers() {
        jwalaUi.click(By.xpath("//button[span[text()='Start Web Servers']]"));
    }

    @And("I start all jvms")
    public void startAllJvms() {
        jwalaUi.click(By.xpath("//button[span[text()='Start JVMs']]"));
    }

    @And("I expanded group \"(.*)\"$")
    public void expandGroup(String groupName) {
        jwalaUi.click(By.xpath("//td[text()='" + groupName + "']/preceding-sibling::td"));
    }

    @And("I wait for popup string \"(.*)\"$")
    public void waitForSomeTime(String value) throws InterruptedException {
        jwalaUi.waitUntilElementIstVisible(By.xpath("//div[contains(text(), '" + value + "')]"), 240000);
    }

    @And("I generate webapp")
    public void generateWebapp() {
        jwalaUi.click(By.xpath("//tr/td[text()='seleniumWebapp']/following-sibling::td[3]/div/button[@title='Generate and deploy the webapp resources.']"));
    }

    @And("I click on ok button")
    public void clickOkButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }
}
