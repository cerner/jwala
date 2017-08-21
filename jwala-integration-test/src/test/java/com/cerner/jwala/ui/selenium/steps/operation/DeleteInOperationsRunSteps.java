package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertFalse;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class DeleteInOperationsRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the delete button of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickWebServerDelete(final String webServerName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[@title='Delete Web Server']"));
    }

    @And("^I click the operation's confirm delete web server dialog yes button$")
    public void clickConfirmWebServerDeleteYesButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @Then("^I see an error dialog box that tells me to stop the web server \"(.*)\"$")
    public void deleteError(final String webServerName) {
        jwalaUi.isElementExists(By.xpath("/contains(text()='Please stop web server " + webServerName +
                " first before attempting to delete it')"));
    }




    /*** code below may need refactoring since steps should be concise to avoid step collisions ***/

    @Then("^I don't see delete error$")
    public void verifyNoError() {
        assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'Please stop')")));
    }

    @Then("^I see element in operations table\"(.*)\"$")
    public void verifyElementPresent(String name) {
        Assert.assertTrue(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }

    @Then("^I don't see \"(.*)\"$")
    public void verifyElementNotPresent(String name) {
        Assert.assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }
}
