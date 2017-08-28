package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Encapsulates run steps related to the control of a particular web server of a certain group and the expected
 * result of such action
 *
 * Created by Jedd Cuison on 8/21/2017
 */
public class WebServerControlRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the \"(.*)\" button of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickControlWebServerBtn(final String buttonTitle, final String webServerName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[@title='" + buttonTitle + "']"));
    }

    @When("^I click the operation's confirm delete web server dialog yes button$")
    public void clickConfirmWebServerDeleteYesButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @When("^I click the \"(.*)\" link of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickWebServerStatusLink(final String linkLabel, final String webServerName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[text()='" + linkLabel + "']"));
    }

    @Then("^I see an error dialog box that tells me to stop the web server \"(.*)\"$")
    public void deleteError(final String webServerName) {
        jwalaUi.isElementExists(By.xpath("/contains(text()='Please stop web server " + webServerName +
                " first before attempting to delete it')"));
    }

    @Then("^I see that \"(.*)\" web server got deleted successfully from the operations tab$")
    public void verifyIfDeleteWebServerIsSuccessful(final String webServerName) {
        jwalaUi.waitUntilElementIsVisible(
                By.xpath("//div[text()='Web server " + webServerName
                        + " was successfully deleted. Jwala will need to refresh to display the latest data and recompute the states.']"));
    }
}
