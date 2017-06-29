package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.steps.configuration.ManageGroupRunSteps;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class CommonRunSteps {

    @Autowired
    private LoginRunSteps loginRunSteps;

    @Autowired
    private ManageGroupRunSteps manageGroupRunSteps;

    @Given("^I logged in$")
    public void logIn() {
        loginRunSteps.loadLoginPage();
        loginRunSteps.enterUserName();
        loginRunSteps.enterPassword();
        loginRunSteps.clickLoginButton();
        loginRunSteps.validateResult();
    }

    @Given("^I created a group with the name \"(.*)\"$")
    public void createGroup(final String groupName) {
        manageGroupRunSteps.goToGroupTab();
        manageGroupRunSteps.clickAddGroupBtn();
        manageGroupRunSteps.checkIfAddGroupDialogBoxIsDisplayed();
        manageGroupRunSteps.setGroupName(groupName);
        manageGroupRunSteps.clickOkBtn();
        manageGroupRunSteps.checkIfGroupWasAdded(groupName);
    }
}
