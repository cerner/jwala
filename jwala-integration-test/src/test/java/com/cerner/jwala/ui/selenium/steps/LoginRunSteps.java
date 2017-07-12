package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.TestConfig;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
<<<<<<< HEAD
=======

>>>>>>> master
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by Jedd Cuison on 6/21/2017
 */
@ContextConfiguration(classes = TestConfig.class)
public class LoginRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am on the login page$")
    public void loadLoginPage() {
        jwalaUi.loadPath("/login");
    }

    @When("^I fill in the \"User Name\" field with a valid user name$")
    public void enterUserName() {
        jwalaUi.sendKeys(By.id("userName"), jwalaUi.getProperties().getProperty("jwala.user.name"));
    }

    @And("^I fill in the \"Password\" field with a valid password$")
    public void enterPassword() {
        jwalaUi.sendKeys(By.id("password"), jwalaUi.getProperties().getProperty("jwala.user.pwd"));
    }

    @And("^I click the login button$")
    public void clickLoginButton() {
        jwalaUi.click(By.cssSelector("input[type=\"button\"]"));
    }

    @Then("^I should see the main page$")
    public void validateResult() {
        jwalaUi.waitUntilElementIsVisible(By.className("banner-logout"));
    }
}
