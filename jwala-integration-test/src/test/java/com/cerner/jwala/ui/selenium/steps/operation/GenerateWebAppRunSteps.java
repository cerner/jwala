package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/14/2017
 */
public class GenerateWebAppRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the generate web application button of \"(.*)\"$")
    @Deprecated
    public void clickGenerateWebAppBtn(final String webAppName) {
        jwalaUi.clickWhenReady(By.xpath("//tr/td[text()='" + webAppName + "']/following-sibling::td[3]/div/button[@title='Generate and deploy the webapp resources.']"));
    }

    @When("^I click the generate web application button of \"(.*)\" web app under group \"(.*)\"$")
    public void clickGenerateWebAppBtnOfGroup(final String webAppName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + webAppName + "']]/following-sibling::tr//td[text()='" + groupName +
                "']/following-sibling::td//button[@title='Generate and deploy the webapp resources.']"));
    }

    @Then("^I see \"(.*)\" web application got deployed successfully$")
    public void checkForSuccessfulResourceDeployment(final String webAppName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='" + webAppName + " resource files deployed successfully']"), 300);
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }
}
