package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class DeleteInOperationsRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the delete button of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickWebServerDelete(final String webServerName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[@title='Delete Web Server']"));
    }

    @When("^I click the delete button of JVM \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickJvmDelete(final String jvmName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//button[@title='Delete JVM']"));
    }

    @And("^I click the operation's confirm delete web server dialog yes button$")
    public void clickConfirmWebServerDeleteYesButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @And("^I click the operation's confirm delete jvm dialog yes button$")
    public void clickConfirmJvmDeleteYesButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @Then("^I see an error dialog box that tells me to stop the web server \"(.*)\"$")
    public void deleteError(final String webServerName) {
        jwalaUi.isElementExists(By.xpath("/contains(text()='Please stop web server " + webServerName +
                " first before attempting to delete it')"));
    }

    @Then("^I don't see an error dialog box that tells me to stop the jvm \"(.*)\"$")
    public void verifyAbsenceOfDeleteError(final String jvmName) {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Please stop JVM " + jvmName +
                " first before attempting to delete it')]"));
    }

    @Then("^I see a popup that tells me about the succesful delete for jvm \"(.*)\" and jwala refresh for operations page$")
    public void verifyRefreshPopup(String jvm) {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'JVM " + jvm + " was successfully deleted. Jwala will need to refresh to display the latest data and recompute the states.')]"));
    }

    @Then("^I click ok on refresh page popup$")
    public void clickOkRefresh() {
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }

    @Then("^I verify element \"(.*)\" is succesfully deleted from group \"(.*)\"$")
    public void verifyElementNotPresent(String elementName, String groupName) {
        jwalaUi.waitUntilElementIsNotVisible(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + elementName + "']"));
    }
}
