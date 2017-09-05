package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.TestConfig;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import com.cerner.jwala.ui.selenium.steps.configuration.*;
import com.cerner.jwala.ui.selenium.steps.operation.*;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
@ContextConfiguration(classes = TestConfig.class)
public class CommonRunSteps {

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Autowired
    private NavigationRunSteps navigationRunSteps;

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

    @Autowired
    private UploadResourceRunSteps uploadResourceRunSteps;

    @Autowired
    private GenerateWebServerRunSteps generateWebServerRunSteps;

    @Autowired
    private StartWebServerRunSteps startWebServerRunSteps;

    @Autowired
    private GenerateWebAppRunSteps generateWebAppRunSteps;

    @Autowired
    private GenerateJvmRunSteps generateJvmRunSteps;

    @Autowired
    private StartJvmRunSteps startJvmRunSteps;

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I logged in$")
    public void logIn() {
        loginRunSteps.loadLoginPage();
        loginRunSteps.enterUserName();
        loginRunSteps.enterPassword();
        loginRunSteps.clickLoginButton();
        loginRunSteps.validateResult();
    }

    public Properties getProperties() {
        return paramProp;
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
        navigationRunSteps.goToMediaTab();
        createMediaRunSteps.clickAddMediaBtn();
        createMediaRunSteps.checkIfAddMediaDialogIsDisplayed();
        createMediaRunSteps.setMediaName(parameters.get("mediaName"));
        createMediaRunSteps.selectMediaType(parameters.get("mediaType"));
        createMediaRunSteps.selectMediaArchiveFile(parameters.get("archiveFilename"));
        createMediaRunSteps.setRemoteDir(getPropertyValue(parameters.get("remoteDir")));
        createMediaRunSteps.clickAddMediaOkDialogBtn();
        createMediaRunSteps.checkForMedia(parameters.get("mediaName"));
    }

    @Given("^I created a web app with the following parameters:$")
    public void createWebApp(final Map<String, String> parameters) {
        navigationRunSteps.goToWebAppsTab();
        createWebAppRunSteps.clickAddWebAppBtn();
        createWebAppRunSteps.checkForWebAppDialog();
        createWebAppRunSteps.setWebAppName(parameters.get("webappName"));
        createWebAppRunSteps.setContextPath(parameters.get("contextPath"));
        List<String> list = new ArrayList<String>();
        list.add(parameters.get("group"));
        createWebAppRunSteps.setGroups(list);
        createWebAppRunSteps.clickAddDialogOkBtn();
        createWebAppRunSteps.checkForWebApp(parameters.get("webappName"));
    }

    @Given("^I created a web server with the following parameters:$")
    public void createWebServer(final Map<String, String> parameters) throws InterruptedException {
        createWebServerRunSteps.goToWebServersTab();
        createWebServerRunSteps.clickAddWebServerBtn();
        createWebServerRunSteps.checkAddWebServerDialogBoxIsDisplayed();
        createWebServerRunSteps.setWebServerName(parameters.get("webserverName"));
        createWebServerRunSteps.setStatusPath(getPropertyValue(parameters.get("statusPath")));
        String hostName = getPropertyValue(parameters.get("hostName"));
        createWebServerRunSteps.setHostName(hostName);
        createWebServerRunSteps.setHttpPort(getPropertyValue(parameters.get("portNumber")));
        createWebServerRunSteps.setHttpsPort(getPropertyValue(parameters.get("httpsPort")));
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
        String hostName = getPropertyValue(parameters.get("hostName"));
        createWebServerRunSteps.setHostName(hostName);
        createJvmRunSteps.setHttpPort(getPropertyValue(parameters.get("portNumber")));
        createJvmRunSteps.selectJdk(parameters.get("jdk"));
        createJvmRunSteps.selectTomcat(parameters.get("tomcat"));
        List<String> groups = new ArrayList<>();
        groups.add(parameters.get("group"));
        createJvmRunSteps.setGroups(groups);
        createJvmRunSteps.clickOkBtn();
        createJvmRunSteps.checkForJvm(parameters.get("jvmName"));
    }

    @Given("^I created a JVM resource with the following parameters:$")
    public void createJvmResources(final Map<String, String> parameters) {
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode("JVMs");
        uploadResourceRunSteps.clickNode(parameters.get("jvm"));
        uploadResourceRunSteps.clickAddResourceBtn();
        uploadResourceRunSteps.setDeployName(parameters.get("deployName"));
        uploadResourceRunSteps.setDeployPath(parameters.get("deployPath"));
        uploadResourceRunSteps.selectResourceFile(parameters.get("templateName"));
        uploadResourceRunSteps.clickUploadResourceDlgOkBtn();
        uploadResourceRunSteps.checkForSuccessfulResourceUpload();
    }

    @Given("^I created a web app resource with the following parameters:$")
    public void createWebAppResources(final Map<String, String> parameters) {
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode("Web Apps");
        uploadResourceRunSteps.clickNode(parameters.get("webApp"));
        uploadResourceRunSteps.clickAddResourceBtn();
        uploadResourceRunSteps.setDeployName(parameters.get("deployName"));
        uploadResourceRunSteps.setDeployPath(parameters.get("deployPath"));
        uploadResourceRunSteps.selectResourceFile(parameters.get("templateName"));
        uploadResourceRunSteps.clickAssignToJvmsCheckbox();
        uploadResourceRunSteps.clickUploadResourceDlgOkBtn();
        uploadResourceRunSteps.checkForSuccessfulResourceUpload();
    }

    @Given("^I created a web server resource with the following parameters:$")
    public void createWebServerResources(final Map<String, String> parameters) {
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode("Web Servers");
        uploadResourceRunSteps.clickNode(parameters.get("webServer"));
        uploadResourceRunSteps.clickAddResourceBtn();
        uploadResourceRunSteps.setDeployName(parameters.get("deployName"));
        uploadResourceRunSteps.setDeployPath(parameters.get("deployPath"));
        uploadResourceRunSteps.selectResourceFile(parameters.get("templateName"));
        uploadResourceRunSteps.clickUploadResourceDlgOkBtn();
        uploadResourceRunSteps.checkForSuccessfulResourceUpload();
    }

    @Given("^I generated the web servers of \"(.*)\" group$")
    public void generateWebServersOfGroup(final String groupName) {
        generateWebServerRunSteps.clickGenerateWebServersBtnOfGroup(groupName);
        generateWebServerRunSteps.checkForTheSuccessfulGenerationOfWebServers(groupName);
    }

    @Given("^I started \"(.*)\" web server of \"(.*)\" group$")
    public void startWebServersOfGroup(final String webServerName, final String groupName) {
        startWebServerRunSteps.clickStartWebServersOfGroup(groupName);
        startWebServerRunSteps.checkIfWebServerStateIsStarted(webServerName, groupName);
    }

    @Given("^I generated the JVMs of \"(.*)\" group$")
    public void generateJvmOfGroup(final String groupName) {
        generateJvmRunSteps.clickGenerateJvmsBtnOfGroup(groupName);
        generateJvmRunSteps.checkForTheSuccessfulGenerationOfJvms(groupName);
    }

    @Given("^I generated \"(.*)\" JVM of \"(.*)\" group$")
    public void generateJvmOfGroup(final String jvmName, final String groupName) {
        generateJvmRunSteps.generateIndividualJvm(jvmName, groupName);
        generateJvmRunSteps.checkForSuccessfulGenerationIndividualJvm();
    }

    @Given("^I started \"(.*)\" JVM of \"(.*)\" group$")
    public void startJvmOfGroup(final String jvmName, final String groupName) {
        startJvmRunSteps.clickStartJvmsOfGroup(groupName);
        startJvmRunSteps.checkIfJvmStateIsStarted(jvmName, groupName);
    }

    @Given("I generated \"(.*)\" web app of \"(.*)\" group$")
    public void generateWebAppOfGroup(final String webAppName, final String groupName) {
        generateWebAppRunSteps.clickGenerateWebAppBtnOfGroup(webAppName, groupName);
        generateWebAppRunSteps.checkForSuccessfulResourceDeployment(webAppName);
    }

    @Then("^I don't see the click status tooltip$")
    public void clickStatusTooltipIsNotVisible() {
        jwalaUi.waitUntilElementIsNotVisible(By.xpath("//div[contains(@class, 'ui-tooltip')]"), 30);
    }

    /**
     * Gets the value from the properties file
     * @param key the property key
     * @return the value of the property, if null the key is returned instead
     */
    private String getPropertyValue(final String key) {
        return paramProp.getProperty(key) == null ? key : paramProp.getProperty(key);
    }
}
