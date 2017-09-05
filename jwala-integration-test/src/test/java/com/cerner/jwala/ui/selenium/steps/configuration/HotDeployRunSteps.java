package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Properties;

/**
 * Created by Sharvari Barve on 9/4/2017.
 */
public class HotDeployRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Then("^I verify and confirm successful deploy$")
    public void verifySuccesfulDeploy() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Deploy successful!')]"));
        clickOktoSuccesfulDeploy();
    }

    @Then("^I verify deploy error message for file \"(.*)\" and jvm \"(.*)\"$")
    public void verifyErrorMessageIndividiualJvm(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target JVM " + jvm + " must be stopped or the resource " + file + " must be set to hotDeploy=true before attempting to update the resource files')]"));
        clickOkToDeployErrorPopup();
    }

    @Then("^I verify deploy error message for ws file \"(.*)\" for webserver \"(.*)\"$")
    public void verifyErrorMessageIndividiualWebServer(String file, String webserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target Web Server " + webserver + " must be stopped or the resource must be configured to be hotDeploy=true before attempting to deploy the resource " + file + "')]"));
        clickOkToDeployErrorPopup();
    }

    @Then("^I verify deploy error message for webserver \"(.*)\" in operations$")
    public void verifyErrorMessageWebserverOperations(String wsName) {
        jwalaUi.isElementExists(By.xpath("The target Web Server " + wsName + " must be stopped before attempting to update the resource file"));
        clickOkToDeployErrorPopup();
    }

    @Then("^I verify deploy error message for jvm \"(.*)\" in operations$")
    public void verifyErrorMessageJVMOperations(String jvmName) {
        jwalaUi.isElementExists(By.xpath("The target JVM " + jvmName + " must be stopped before attempting to update the resource file"));
        clickOkToDeployErrorPopup();

    }

    @Then("^I verify error message for group \"(.*)\" for jvm file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageGroupJvm(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Failed to deploy file " + file + " for group " + group + " since the following JVMs are running and the file is not configured for hot deploy: [" + jvm + "]')]"));
        clickOkToDeployErrorPopup();
    }

    @Then("^I verify ws error message for group \"(.*)\" for ws file \"(.*)\" with one of Web-servers as \"(.*)\"$")
    public void verifyErrorMessageGroupWebServer(String group, String file, String ws) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy " + file + " for group " + group + ": the following Web Servers were started and the resource was not configured for hotDeploy=true: [" + ws + "]')]"));
        clickOkToDeployErrorPopup();
    }

    @Then("I verify error message for group \"(.*)\" for app file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageIndividualApp(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy file " + file + " for group " + group + ": not all JVMs were stopped - the following JVMs were started and the resource was not configured with hotDeploy=true: [" + jvm + "]')]"));
        clickOkToDeployErrorPopup();
    }

    @Then("^I verify webapp generate error in operations for jvm \"(.*)\"$")
    public void verifyAppOperationsDeployError(String jvmName) {
        jwalaUi.isElementExists(By.xpath("//div[contains(text(),'Make sure the following JVMs are completely stopped before deploying.')]"));
        jwalaUi.isElementExists(By.xpath("//div[text()='" + jvmName + "'"));
        clickOkToDeployErrorPopup();
    }

    @And("^I click ok to resource deploy error message$")
    public void clickOkToDeployErrorPopup() {
        clickOkButton();
    }

    @And("^I click ok to succesful deploy message$")
    public void clickOktoSuccesfulDeploy() {
        clickOkButton();
    }

    @And("^I click the ok button to override JVM MetaData of group webapps$")
    public void clickOkToOverride() {
        clickOkButton();
    }

    @Then("^I verify and confirm that webapp \"([^\"]*)\" is succesfully deployed in Operations page$")
    public void verifySuccesfulOperationsDeploy(String webappName) throws Throwable {
        jwalaUi.isElementExists(By.xpath("//*[text()='"+webappName+" resource files deployed successfully'"));
        clickOktoSuccesfulDeploy();

    }

    @Then("^I verify that jvm resource files are successfuly deployed in Operations page$")
    public void VerifyAndConfirmThatJvmResourceFilesAreSuccessfulyDeployedInOperationsPage() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Successfully generated and deployed JVM resource files')]"));
        clickOktoSuccesfulDeploy();
    }

    @Then("^I verify that webserver resource files are successfuly deployed in Operations page$")
    public void VerifyAndConfirmThatWebserverResourceFilesAreSuccessfulyDeployedInOperationsPage() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[(contains(text(),'Successfully installed the service, and generated and deployed configuration file(s).')]"));
        clickOktoSuccesfulDeploy();
    }

    private void clickOkButton(){
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }
}


