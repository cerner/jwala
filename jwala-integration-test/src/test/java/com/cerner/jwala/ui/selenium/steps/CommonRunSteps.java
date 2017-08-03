package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.steps.configuration.*;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.cerner.jwala.ui.selenium.SeleniumTestHelper.getJwalaProperties;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class CommonRunSteps {

    private Properties prop = new Properties();

    @Autowired
    private LoginRunSteps loginRunSteps;

    @Autowired
    private CreateGroupRunSteps createGroupRunSteps;

    @Autowired
    private CreateMediaRunSteps createMediaRunSteps;

    @Autowired
    private CreateWebAppRunSteps createWebAppRunSteps;

    @Autowired
    private CreateWebServerRunSteps createWebServerRunSteps;

    @Autowired
    private CreateJvmRunSteps createJvmRunSteps;

    @Given("^I logged in$")
    public void logIn() {
        loginRunSteps.loadLoginPage();
        loginRunSteps.enterUserName();
        loginRunSteps.enterPassword();
        loginRunSteps.clickLoginButton();
        loginRunSteps.validateResult();
    }

    @Given("^I load properties file$")
    public void loadPropertiesFile() throws IOException {
        prop = getJwalaProperties();
    }

    public Properties getProperties() {
        return prop;
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
        createMediaRunSteps.setRemoteDir(prop.getProperty(parameters.get("remoteDir")));
        createMediaRunSteps.clickAddMediaOkDialogBtn();
        createMediaRunSteps.checkForMedia(parameters.get("mediaName"));
    }

    @Given("^I created a web app with the following parameters:$")
    public void createWebApp(final Map<String, String> parameters) {
        createWebAppRunSteps.goToWebAppsTab();
        createWebAppRunSteps.clickAddWebAppBtn();
        createWebAppRunSteps.checkForWebAppDialog();
        createWebAppRunSteps.setWebAppName(parameters.get("webappName"));
        createWebAppRunSteps.setContextPath(parameters.get("contextPath"));
        List<String> list = new ArrayList<String>();
        list.add(parameters.get("group"));
        createWebAppRunSteps.setGroups(list);
        createWebAppRunSteps.clickAddDialogOkBtn();
    }

    @Given("^I created a web server with the following parameters:$")
    public void createWebServer(final Map<String, String> parameters) throws InterruptedException {
        createWebServerRunSteps.goToWebServersTab();
        createWebServerRunSteps.clickAddWebServerBtn();
        createWebServerRunSteps.checkAddWebServerDialogBoxIsDisplayed();
        createWebServerRunSteps.setWebServerName(parameters.get("webserverName"));
        createWebServerRunSteps.setStatusPath(parameters.get("statusPath"));
        String hostName = prop.getProperty(parameters.get("hostName")) == null ? parameters.get("hostName") : prop.getProperty(parameters.get("hostName"));
        createWebServerRunSteps.setHostName(hostName);
        createWebServerRunSteps.setHttpPort(parameters.get("portNumber"));
        createWebServerRunSteps.setHttpsPort(parameters.get("httpsPort"));
        createWebServerRunSteps.selectApacheHttpd(parameters.get("apacheHttpdMediaId"));
        createWebServerRunSteps.selectGroup(parameters.get("group"));
        createWebServerRunSteps.clickAddWebServerDialogOkBtn();
        createWebServerRunSteps.checkForWebServer(parameters.get("webserverName"));
    }

    @Given("^I created a jvm with the following parameters:$")
    public void createJvm(final Map<String, String> parameters) throws InterruptedException {
        createJvmRunSteps.goToJvmTab();
        createJvmRunSteps.clickAddJvmBtn();
        createJvmRunSteps.checkForAddJvmDlg();
        createJvmRunSteps.setName(parameters.get("jvmName"));
        createJvmRunSteps.clickStatusPath();
        String hostName = prop.getProperty(parameters.get("hostName")) == null ? parameters.get("hostName") : prop.getProperty(parameters.get("host"));
        createWebServerRunSteps.setHostName(hostName);
        createJvmRunSteps.setHttpPort(parameters.get("portNumber"));
        createJvmRunSteps.selectJdk(parameters.get("jdk"));
        createJvmRunSteps.selectTomcat(parameters.get("tomcat"));
        List<String> groups = new ArrayList<>();
        groups.add(parameters.get("group"));
        createJvmRunSteps.setGroups(groups);
        createJvmRunSteps.clickOkBtn();
        createJvmRunSteps.waitForJvm(parameters.get("jvmName"));

    }


}
