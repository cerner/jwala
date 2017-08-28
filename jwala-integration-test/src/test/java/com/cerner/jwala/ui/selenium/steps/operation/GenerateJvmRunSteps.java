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
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//button[span[text()='Generate JVMs']]"));
    }

    @Then("^I see that the JVMs were successfully generated for group \"(.*)\"$")
    public void checkForTheSuccessfulGenerationOfJvms(final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully generated the JVMs for " + groupName + "']"), 1200);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @When("^I generate \"(.*)\" JVM of \"(.*)\" group$")
    public void generateIndividualJvm(final String jvm, String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='" + jvm + "']/following-sibling::td//button[@title='Generate JVM resources files and deploy as a service']"));
    }

    @Then("^I see the JVM was successfully generated$")
    public void checkForSuccessfulGenerationIndividualJvm() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully generated and deployed JVM resource files']"), 1200);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }
}
