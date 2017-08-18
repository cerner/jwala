package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/18/2017
 */
public class StopJvmRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("I click \"Stop JVMs\" button of group \"(.*)\"")
    public void clickStopJvmsOfGroup(final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName +
                "']]/following-sibling::tr//button[span[text()='Stop JVMs']]"));
    }

    @Then("I see the state of \"(.*)\" JVM of group \"(.*)\" is \"STOPPED\"")
    public void checkIfJvmStateIsStopped(final String jvmName, final String groupName) {
        // used contains here to check for STOP since there are 2 stopped states e.g.
        // STOPPED and FORCED_STOPPED
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//span[contains(text(), 'STOP')]"), 120);
    }
}
