package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class ManageGroupRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the group tab$")
    public void goToGroupTab() {
        jwalaUi.sleep();
        jwalaUi.clickTab("Group");
    }

    @When("^I click the add group button$")
    public void clickAddGroupBtn() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Add']]"));
    }

    @And("^I see the group add dialog$")
    public void checkIfAddGroupDialogBoxIsDisplayed() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add Group']"));
    }

    @And("^I fill in the \"Group Name\" field with \"(.*)\"$")
    public void setGroupName(final String groupName) {
        jwalaUi.sendKeys(By.xpath("//input[@name='name']"), groupName);
    }

    @And("^I click the group add dialog ok button$")
    public void clickOkBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @Then("^I see \"(.*)\" in the group table$")
    public void checkIfGroupWasAdded(final String groupName) {
        jwalaUi.waitUntilNumberOfElementsToBe(By.xpath("//button[text()='" + groupName + "']"), 1);
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
