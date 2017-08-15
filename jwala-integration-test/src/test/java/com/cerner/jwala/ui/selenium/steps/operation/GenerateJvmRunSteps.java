package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/14/2017
 */
public class GenerateJvmRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the \"Generate JVMs\" button of group \"(.*)\"$")
    public void clickGenerateJvmsBtnOfGroup(final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//button[span[text()='Generate JVMs']]"));
    }

    @Then("^I see that the JVMs were successfully generated for group \"(.*)\"$")
    public void checkForTheSuccessfulGenerationOfJvms(final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully generated the JVMs for " + groupName + "']"), 600);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
        jwalaUi.sleep();
    }
}
