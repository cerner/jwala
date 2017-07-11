package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.steps.configuration.*;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private ManageJvmRunSteps manageJvmRunSteps;

    @Autowired
    private ManageWebAppRunSteps manageWebAppRunSteps;

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

    @Given("I created a jvm with following parameters:$")
    public void createJvm(final Map<String, String> parameters) {
        manageJvmRunSteps.goToJvmTab();
        manageJvmRunSteps.clickAddJvmBtn();
        manageJvmRunSteps.checkForAddJvmDlg();
        manageJvmRunSteps.setName(parameters.get("jvmName"));
        manageJvmRunSteps.setHostName(parameters.get("hostName"));
        manageJvmRunSteps.setHttpPort(parameters.get("portNumber"));
        manageJvmRunSteps.selectJdk(parameters.get("jdk"));
        manageJvmRunSteps.selectTomcat(parameters.get("tomcat"));
        manageJvmRunSteps.clickStatusPath();
        List<String> groups = new ArrayList<String>();
        groups.add(parameters.get("group"));
        manageJvmRunSteps.setGroups(groups);
        manageJvmRunSteps.clickOkBtn();
    }

    @Given("I created a webapp with following parameters:$")
    public void createWebapp(final Map<String, String> parameters) {
        manageWebAppRunSteps.goToWebAppsTab();
        manageWebAppRunSteps.clickAddWebAppBtn();
        manageWebAppRunSteps.checkForWebAppDialog();
        manageWebAppRunSteps.setWebAppName(parameters.get("webappName"));
        manageWebAppRunSteps.setContextPath(parameters.get("contextPath"));
        List<String> groups = new ArrayList<String>();
        groups.add(parameters.get("group"));
        manageWebAppRunSteps.setGroups(groups);
        manageWebAppRunSteps.clickAddDialogOkBtn();
    }

}
