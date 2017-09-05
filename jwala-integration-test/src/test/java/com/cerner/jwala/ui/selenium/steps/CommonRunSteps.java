package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.TestConfig;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import com.cerner.jwala.ui.selenium.steps.configuration.*;
import com.cerner.jwala.ui.selenium.steps.operation.*;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import cucumber.api.java.en.When;
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

    @Autowired
    private HandleResourceRunSteps handleResourceRunSteps;

    @Autowired
    private ResourceErrorHandlingRunSteps resourceErrorHandlingRunSteps;


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

    @And("^I generate and start the webserver with the following parameters:$")
    public void generateAndStartWebserver(Map<String, String> parameters) {
        navigationRunSteps.goToOperationsTab();
        navigationRunSteps.expandGroupInOperationsTab(parameters.get("group"));
        generateWebServerRunSteps.generateIndividualWebserver(parameters.get("webserverName"), parameters.get("group"));
        generateWebServerRunSteps.checkForSuccessfulGenerationOfAWebserver();
        startWebServersOfGroup(parameters.get("webserverName"), parameters.get("group"));
        startWebServerRunSteps.checkIfWebServerStateIsStarted(parameters.get("webserverName"), parameters.get("group"));
    }

    @And("^I generate and start the jvm with the following parameters:$")
    public void generateAndStartJvm(Map<String, String> parameters) {
        navigationRunSteps.goToOperationsTab();
        navigationRunSteps.expandGroupInOperationsTab(parameters.get("group"));
        generateJvmRunSteps.generateIndividualJvm(parameters.get("jvmName"), parameters.get("group"));
        generateJvmRunSteps.checkForSuccessfulGenerationIndividualJvm();
        startJvmOfGroup(parameters.get("jvmName"), parameters.get("group"));
        startJvmRunSteps.checkIfJvmStateIsStarted(parameters.get("jvmName"), parameters.get("group"));
    }

    /*
      The methods below don't have the check for success, so we can use them both for error checking and succesfuly deploy
     */
    @And("^I try to generate webserver with the following parameters:$")
    public void attemptToGenerateWebserver(Map<String, String> parameters) {
        navigationRunSteps.goToOperationsTab();
        navigationRunSteps.expandGroupInOperationsTab(parameters.get("group"));
        generateWebServerRunSteps.generateIndividualWebserver(parameters.get("webserverName"), parameters.get("group"));
    }

    @And("^I try to generate jvm with the following parameters:$")
    public void attemptToGenerateJvm(Map<String, String> parameters) {
        navigationRunSteps.goToOperationsTab();
        navigationRunSteps.expandGroupInOperationsTab(parameters.get("group"));
        generateJvmRunSteps.generateIndividualJvm(parameters.get("jvmName"), parameters.get("group"));
    }

    @And("^I try to generate the webapp with the following parameters:$")
    public void attemptToGenerateWebapp(Map<String, String> parameters) {
        navigationRunSteps.goToOperationsTab();
        navigationRunSteps.expandGroupInOperationsTab(parameters.get("group"));
        generateWebAppRunSteps.clickGenerateWebAppBtnOfGroup(parameters.get("webAppName"), parameters.get("group"));
    }

    @When("^I enter text in resource edit box and save with the following parameters:$")
    public void enterValueAndSave(Map<String, String> parameters) {
        handleResourceRunSteps.clickResource(parameters.get("fileName"));
        handleResourceRunSteps.clickTab(parameters.get("tabLabel"));
        handleResourceRunSteps.enterInEditBox(parameters.get("text"), parameters.get("position"));
        handleResourceRunSteps.clickSaveButton(parameters.get("tabLabel"));
        //not included waiting for saved notification here as if there is an error ,we get an error instead of notification
        //and if we include a condition to check for error, by the time of checking is completed, the notification disappears
    }

    @When("^I delete the line in the resource file with the following parameters:$")
    public void deleteLine(Map<String, String> parameters) {
        handleResourceRunSteps.clickResource(parameters.get("fileName"));
        handleResourceRunSteps.clickTab(parameters.get("tabLabel"));
        //needed to save chrome popup from an unsaved file
        resourceErrorHandlingRunSteps.removeGarbageValue(parameters.get("textLine"));
        handleResourceRunSteps.clickSaveButton(parameters.get("tabLabel"));
    }

    @And("^I enter attribute in the file MetaData with the following parameters:$")
    public void enterAttributeInMetaData(Map<String, String> parameters) {
        navigationRunSteps.goToConfigurationTab();
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode(parameters.get("componentType"));
        uploadResourceRunSteps.clickNode(parameters.get("componentName"));
        handleResourceRunSteps.clickResource(parameters.get("fileName"));
        handleResourceRunSteps.clickTab("Meta Data");
        handleResourceRunSteps.enterAttribute(parameters.get("attributeKey"), parameters.get("attributeValue"));
        handleResourceRunSteps.clickSaveButton("Meta Data");
        if(parameters.get("override").equals("true")){
            handleResourceRunSteps.clickOkToOverride();
        }
        handleResourceRunSteps.waitForNotification("Saved");
    }

    /**
     * Gets the value from the properties file
     *
     * @param key the property key
     * @return the value of the property, if null the key is returned instead
     */
    private String getPropertyValue(final String key) {
        return paramProp.getProperty(key) == null ? key : paramProp.getProperty(key);
    }

    @And("^I go to the file in resources with the following parameters:$")
    public void goToResource(Map<String, String> parameters) {
        navigationRunSteps.goToConfigurationTab();
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode(parameters.get("componentType"));
        uploadResourceRunSteps.clickNode(parameters.get("componentName"));
        handleResourceRunSteps.selectFile(parameters.get("fileName"));

    }

    @And("^I created a group JVM resource with the following parameters:$")
    public void createGroupJvmResource(Map<String,String> parameters) throws Throwable {
        navigationRunSteps.goToResourceTab();
        uploadResourceRunSteps.expandNode(parameters.get("group"));
        uploadResourceRunSteps.expandNode("JVMs");
        uploadResourceRunSteps.clickNode("JVMS");
        uploadResourceRunSteps.clickAddResourceBtn();
        uploadResourceRunSteps.setDeployName(parameters.get("deployName"));
        uploadResourceRunSteps.setDeployPath(parameters.get("deployPath"));
        uploadResourceRunSteps.selectResourceFile(parameters.get("templateName"));
        uploadResourceRunSteps.clickUploadResourceDlgOkBtn();
        uploadResourceRunSteps.checkForSuccessfulResourceUpload();
    }
}
