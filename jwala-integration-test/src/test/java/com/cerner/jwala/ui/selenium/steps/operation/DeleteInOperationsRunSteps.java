package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
@Deprecated
public class DeleteInOperationsRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Then("^I see element in operations table\"(.*)\"$")
    public void verifyElementPresent(String name) {
        Assert.assertTrue(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }

    @Then("^I don't see \"(.*)\"$")
    public void verifyElementNotPresent(String name) {
        Assert.assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }

    @Then("^No resource is present$")
    public void absenceOfResource(){
        jwalaUi.find(By.xpath("//*[contains(text(), 'No resources found...')]"));    }
}
