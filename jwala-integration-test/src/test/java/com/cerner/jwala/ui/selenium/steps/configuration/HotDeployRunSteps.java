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

    @Then("^I confirm successful deploy popup$")
    public void verifySuccesfulDeploy() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Deploy successful!')]"));
        clickOkButton();
    }

    @Then("^I confirm deploy error message popup for file \"(.*)\" and jvm \"(.*)\"$")
    public void verifyErrorMessageIndividiualJvm(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target JVM " + jvm + " must be stopped or the resource " + file + " must be set to hotDeploy=true before attempting to update the resource files')]"));
        clickOkButton();
    }

    @Then("^I confirm deploy error message popup for ws file \"(.*)\" for webserver \"(.*)\"$")
    public void verifyErrorMessageIndividiualWebServer(String file, String webserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target Web Server " + webserver + " must be stopped or the resource must be configured to be hotDeploy=true before attempting to deploy the resource " + file + "')]"));
        clickOkButton();
    }

    @Then("^I confirm deploy error message popup for webserver \"(.*)\" in operations$")
    public void verifyErrorMessageWebserverOperations(String wsName) {
        jwalaUi.isElementExists(By.xpath("The target Web Server " + wsName + " must be stopped before attempting to update the resource file"));
        clickOkButton();
    }

    @Then("^I confirm deploy error message popup for jvm \"(.*)\" in operations$")
    public void verifyErrorMessageJVMOperations(String jvmName) {
        jwalaUi.isElementExists(By.xpath("The target JVM " + jvmName + " must be stopped before attempting to update the resource file"));
        clickOkButton();

    }

    @Then("^I confirm error message popup for group \"(.*)\" for jvm file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageGroupJvm(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Failed to deploy file " + file + " for group " + group + " since the following JVMs are running and the file is not configured for hot deploy: [" + jvm + "]')]"));
        clickOkButton();
    }

    @Then("^I verify ws error message popup for group \"(.*)\" for ws file \"(.*)\" with one of Web-servers as \"(.*)\"$")
    public void verifyErrorMessageGroupWebServer(String group, String file, String ws) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy " + file + " for group " + group + ": the following Web Servers were started and the resource was not configured for hotDeploy=true: [" + ws + "]')]"));
        clickOkButton();
    }

    @Then("I verify error message popup for group \"(.*)\" for app file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageIndividualApp(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy file " + file + " for group " + group + ": not all JVMs were stopped - the following JVMs were started and the resource was not configured with hotDeploy=true: [" + jvm + "]')]"));
        clickOkButton();
    }

    @Then("^I confirm webapp generate error popup in operations for jvm \"(.*)\"$")
    public void verifyAppOperationsDeployError(String jvmName) {
        jwalaUi.isElementExists(By.xpath("//div[contains(text(),'Make sure the following JVMs are completely stopped before deploying.')]"));
        jwalaUi.isElementExists(By.xpath("//div[text()='" + jvmName + "'"));
        clickOkButton();
    }

    @Then("^I confirm webapp \"([^\"]*)\" is succesfully deployed in Operations page popup$")
    public void verifySuccesfulOperationsDeploy(String webappName) throws Throwable {
        jwalaUi.isElementExists(By.xpath("//*[text()='"+webappName+" resource files deployed successfully'"));
        clickOkButton();

    }

    @Then("^I confirm jvm resource files are successfuly deployed in Operations page popup$")
    public void VerifyAndConfirmThatJvmResourceFilesAreSuccessfulyDeployedInOperationsPage() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Successfully generated and deployed JVM resource files')]"));
        clickOkButton();
    }

    @Then("^I confirm webserver resource files are successfuly deployed in Operations page popup$")
    public void VerifyAndConfirmThatWebserverResourceFilesAreSuccessfulyDeployedInOperationsPage() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[(contains(text(),'Successfully installed the service, and generated and deployed configuration file(s).')]"));
        clickOkButton();
    }

    private void clickOkButton(){
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }
}


