package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Sharvari Barve on 7/6/2017.
 */
public class ManageSortRunSteps {
    @Autowired
    private JwalaUi jwalaUi;


    @When("^I click on the sort button of component \"(.*)\" with attribute \"(.*)\"$")
    public void clickSortButton(String component, String attribute) {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(text(), '" + component + attribute + "')]"));
    }

    @When("^I click on the sort button with attribute \"(.*)\"$")
    public void clickSortButtonWithAttribute(String attribute) {
        jwalaUi.click(By.xpath("//div[text()='" + attribute + "']/.."));
    }

    @Then("^I see first item \"(.*)\"$")
    public void checkFirstElement(String firstElementName) {
        List<WebElement> listOfWebElements = jwalaUi.getWebDriver().findElements(By.xpath("//td[button[@class='button-link']]"));
        WebElement firstElement = listOfWebElements.get(0);
        assertEquals(firstElement.getText(), firstElementName);
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

}