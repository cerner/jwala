package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Created by Sharvari Barve on 8/14/2017.
 */
public class ResourceDeleteRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click resource file delete button$")
    public void clickDelete(){
        jwalaUi.click(By.xpath("//*[@title='delete']"));
    }

}
