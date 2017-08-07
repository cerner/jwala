package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;import java.io.IOException;import java.sql.SQLException;

/**
 * Created by SB053052 on 7/18/2017.
 */
public class DeleteInOperationsRunSteps {
    @Autowired
    JwalaUi jwalaUi;

    @And("I click on yes button")
    public void clickOkButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @Then("I don't see \"(.*)\"$")
    public void verifyElementNotPresent(String name){
        Assert.assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'"+name+"')")));
    }
    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
