package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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

    @And("^I expand the group operation's \"(.*)\" group$")
    public void expandGroup(String groupName) {
        jwalaUi.click(By.xpath("//td[text()='" + groupName + "']/preceding-sibling::td"));
    }

    @When("^I click the generate web application button of \"(.*)\"$")
    public void generateWebapp(final String webAppName) {
        jwalaUi.click(By.xpath("//tr/td[text()='" + webAppName + "']/following-sibling::td[3]/div/button[@title='Generate and deploy the webapp resources.']"));
    }

    @Then("^I see \"(.*)\" web application got deployed successfully$")
    public void checkForSuccessfulResourceDeployment(final String entityName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='" + entityName + " resource files deployed successfully']"), 300);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @When("^I click the \"Generate Web Servers\" button of group \"(.*)\"$")
    public void generateAllWebservers(final String groupName) {
        jwalaUi.click(By.xpath("//td[text()='" + groupName + "']/parent::tr/following-sibling::tr[1]/td[1]/div[2]/div[1]/div[1]/div[2]/button"));
    }

    @Then("^I see that the web servers were successfully generated for group \"(.*)\"$")
    public void checkForTheSuccessfulGenerationOfWebServers(final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully generated the web servers for " + groupName + "']"), 300);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }
}
