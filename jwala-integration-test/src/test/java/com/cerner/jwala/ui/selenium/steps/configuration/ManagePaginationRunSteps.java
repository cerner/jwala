package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by SB053052 on 7/10/2017.
 */
public class ManagePaginationRunSteps {

@Autowired
    JwalaUi jwalaUi;

    @When("I click the right button")
    public void clickRightButton(){
        jwalaUi.clickWhenReady(By.className("ui-corner-right"));
        jwalaUi.sleep();

    }

    @When("I click the left button")
    public void clickLeftButton(){
        jwalaUi.clickWhenReady(By.className("ui-corner-left"));
        jwalaUi.sleep();
    }

    @Then("I see the text \"(.*)\"$")
    public void verifyText(String text){
       WebElement element = jwalaUi.getWebDriver().findElement(By.xpath("//*[contains(text(),'" + text + "')]"));
       assertNotNull(element);
    }


    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
