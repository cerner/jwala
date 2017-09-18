package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/28/2017
 */
public class JvmControlRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the \"(.*)\" button of JVM \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickControlJvmBtn(final String buttonTitle, final String jvmName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//button[@title='" + buttonTitle + "']"));
    }

    @When("^I click the operation's confirm delete JVM dialog yes button$")
    public void clickConfirmJvmDeleteYesButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @Then("^I see that \"(.*)\" JVM got deleted successfully from the operations tab$")
    public void verifyIfDeleteJvmIsSuccessful(final String jvmName) {
        jwalaUi.waitUntilElementIsVisible(
                By.xpath("//div[text()='JVM " + jvmName +
                        " was successfully deleted. Jwala will need to refresh to display the latest data and recompute the states.']"));
    }
}
