package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/15/2017
 */
public class StopWebServerRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("I click \"Stop Web Servers\" button of group \"(.*)\"")
    public void clickStopWebServersOfGroup(final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName +
                "']]/following-sibling::tr//button[span[text()='Stop Web Servers']]"));
    }

    @Then("I see the state of \"(.*)\" web server of group \"(.*)\" is \"STOPPED\"")
    public void checkIfWebServerStateIsStopped(final String webServerName, final String groupName) {
        // used contains here to check for STOP since there are 2 stopped states e.g.
        // STOPPED and FORCED_STOPPED
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//span[contains(text(), 'STOP')]"), 120);
    }
}
