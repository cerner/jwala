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
    private CreateGroupRunSteps createGroupRunSteps;

    @Autowired
    private CreateWebServerRunSteps createWebServerRunSteps;

    @Autowired
    private CreateJvmRunSteps createJvmRunSteps;

    @Autowired
    private CreateWebAppRunSteps createWebAppRunSteps;

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

    @Given("^I created a webserver with following parameters:$")
    public void createWebServer(final Map<String, String> parameters) throws InterruptedException {
        createWebServerRunSteps.goToWebServersTab();
        createWebServerRunSteps.clickAddWebServerBtn();
        createWebServerRunSteps.checkAddWebServerDialogBoxIsDisplayed();
        createWebServerRunSteps.setWebServerName(parameters.get("webserverName"));
        createWebServerRunSteps.setStatusPath(parameters.get("statusPath"));
        createWebServerRunSteps.setHostName(parameters.get("hostName"));
        createWebServerRunSteps.setHttpPort(parameters.get("portNumber"));
        createWebServerRunSteps.setHttpsPort(parameters.get("httpsPort"));
        createWebServerRunSteps.selectApacheHttpd(parameters.get("apacheHttpdMediaId"));
        createWebServerRunSteps.selectGroup(parameters.get("group"));
        createWebServerRunSteps.clickAddWebServerDialogOkBtn();
    }

    @Given("^I created a jvm with following parameters:$")
    public void createJvm(final Map<String, String> parameters) {
        createJvmRunSteps.goToJvmTab();
        createJvmRunSteps.clickAddJvmBtn();
        createJvmRunSteps.checkForAddJvmDlg();
        createJvmRunSteps.setName(parameters.get("jvmName"));
        createJvmRunSteps.setHostName(parameters.get("hostName"));
        createJvmRunSteps.setHttpPort(parameters.get("portNumber"));
        createJvmRunSteps.selectJdk(parameters.get("jdk"));
        createJvmRunSteps.selectTomcat(parameters.get("tomcat"));
        createJvmRunSteps.clickStatusPath();
        List<String> groups = new ArrayList<String>();
        groups.add(parameters.get("group"));
        createJvmRunSteps.setGroups(groups);
        createJvmRunSteps.clickOkBtn();
    }

    @Given("^I created a webapp with following parameters:$")
    public void createWebapp(final Map<String, String> parameters) {
        createWebAppRunSteps.goToWebAppsTab();
        createWebAppRunSteps.clickAddWebAppBtn();
        createWebAppRunSteps.checkForWebAppDialog();
        createWebAppRunSteps.setWebAppName(parameters.get("webappName"));
        createWebAppRunSteps.setContextPath(parameters.get("contextPath"));
        List<String> groups = new ArrayList<String>();
        groups.add(parameters.get("group"));
        createWebAppRunSteps.setGroups(groups);
        createWebAppRunSteps.clickAddDialogOkBtn();
    }

}
