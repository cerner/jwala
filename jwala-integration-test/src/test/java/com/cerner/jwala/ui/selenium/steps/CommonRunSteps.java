package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.steps.configuration.CreateGroupRunSteps;
import com.cerner.jwala.ui.selenium.steps.configuration.CreateMediaRunSteps;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class CommonRunSteps {

    @Autowired
    private LoginRunSteps loginRunSteps;

    @Autowired
    private CreateGroupRunSteps createGroupRunSteps;

    @Autowired
    private CreateMediaRunSteps createMediaRunSteps;

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
        createGroupRunSteps.goToGroupTab();
        createGroupRunSteps.clickAddGroupBtn();
        createGroupRunSteps.checkIfAddGroupDialogBoxIsDisplayed();
        createGroupRunSteps.setGroupName(groupName);
        createGroupRunSteps.clickOkBtn();
        createGroupRunSteps.checkIfGroupWasAdded(groupName);
    }

    @Given("^I created a media with the following parameters:$")
    public void createMedia(final Map<String, String> parameters) {
        createMediaRunSteps.goToMediaTab();
        createMediaRunSteps.clickAddMediaBtn();
        createMediaRunSteps.checkIfAddMediaDialogIsDisplayed();
        createMediaRunSteps.setMediaName(parameters.get("mediaName"));
        createMediaRunSteps.selectMediaType(parameters.get("mediaType"));
        createMediaRunSteps.selectMediaArchiveFile(parameters.get("archiveFilename"));
        createMediaRunSteps.setRemoteDir(parameters.get("remoteDir"));
        createMediaRunSteps.clickAddMediaOkDialogBtn();
        createMediaRunSteps.checkForMedia(parameters.get("mediaName"));
    }
}
