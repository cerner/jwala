package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.steps.configuration.ManageGroupRunSteps;
import com.cerner.jwala.ui.selenium.steps.configuration.ManageMediaRunSteps;
import com.cerner.jwala.ui.selenium.steps.configuration.ManageWebServerRunSteps;
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
    private ManageGroupRunSteps manageGroupRunSteps;

    @Autowired
    private ManageMediaRunSteps manageMediaRunSteps;

    @Autowired
    private ManageWebServerRunSteps manageWebServerRunSteps;

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

    @Given("^I created a media with the following parameters:$")
    public void createMedia(final Map<String, String> parameters) {
        manageMediaRunSteps.goToMediaTab();
        manageMediaRunSteps.clickAddMediaBtn();
        manageMediaRunSteps.checkIfAddMediaDialogIsDisplayed();
        manageMediaRunSteps.setMediaName(parameters.get("mediaName"));
        manageMediaRunSteps.selectMediaType(parameters.get("mediaType"));
        manageMediaRunSteps.selectMediaArchiveFile(parameters.get("archiveFilename"));
        manageMediaRunSteps.setRemoteDir(parameters.get("remoteDir"));
        manageMediaRunSteps.clickAddMediaOkDialogBtn();
        manageMediaRunSteps.checkForMedia(parameters.get("mediaName"));
    }

    @Given("I created a webserver with following parameters:$")
    public void createWebServer(final Map<String, String> parameters) throws InterruptedException {
        createMedia(parameters);
        manageWebServerRunSteps.goToWebServersTab();
        manageWebServerRunSteps.clickAddWebServerBtn();
        manageWebServerRunSteps.checkAddWebServerDialogBoxIsDisplayed();
        manageWebServerRunSteps.setWebServerName(parameters.get("webserverName"));
        manageWebServerRunSteps.selectStatusPath();
        manageWebServerRunSteps.setHostName(parameters.get("hostName"));
        manageWebServerRunSteps.setHttpPort(parameters.get("portNumber"));
        manageWebServerRunSteps.setHttpsPort(parameters.get("httpsPort"));
        manageWebServerRunSteps.selectApacheHttpd(parameters.get("apacheHttpdMediaId"));
        manageWebServerRunSteps.selectGroup(parameters.get("group"));
        manageWebServerRunSteps.clickAddWebServerDialogOkBtn();
    }

}
