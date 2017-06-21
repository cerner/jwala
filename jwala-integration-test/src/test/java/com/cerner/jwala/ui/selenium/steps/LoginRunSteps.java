package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;

import static org.junit.Assert.fail;

/**
 * Created by Jedd Cuison on 6/21/2017
 */
public class LoginRunSteps extends JwalaTest {

    @Given("^I am on the login page$")
    public void loginPage() {
        driver.get(getBaseUrl() + "/login");
    }

    @When("^When I fill in the \"User Name\" field with \"(.*)\"$")
    public void enterUserName(final String userName) {
        driver.findElement(By.id("userName")).sendKeys(userName);
    }

    @And("^I fill in the \"Password\" field with \"(.*)\"$")
    public void enterPassword(final String password) {
        driver.findElement(By.id("password")).sendKeys(password);
    }

    @And("^I click the login button$")
    public void clickLoginButton() {
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
    }

    @When("^I should see the \"(.*)\"$")
    public void result(final String result) {
        if (result.equalsIgnoreCase("mainPage")) {
            waitUntilElementIsVisible(By.className("banner-logout"));
        } else if (result.equalsIgnoreCase("loginErrorMessage")) {
            waitUntilElementIsVisible(By.className("login-error-msg"));
        } else {
            fail("Unexpected result = " + result);
        }
    }

}
