package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/18/2017
 */
public class StartJvmRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("I click \"Start JVMs\" button of group \"(.*)\"")
    public void clickStartJvmsOfGroup(final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName +
                "']]/following-sibling::tr//button[span[text()='Start JVMs']]"));
    }

    @Then("I see the state of \"(.*)\" JVM of group \"(.*)\" is \"STARTED\"")
    public void checkIfJvmStateIsStarted(final String jvmName, final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//span[text()='STARTED']"), 120);
    }

    @When("^I click start on jvm \"(.*)\" of the group \"(.*)\"$")
    public void clickStartIndvidualJvm(final String jvmName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"+ jvmName + "']/following-sibling::td//button[@title='Start']"));    }

}
