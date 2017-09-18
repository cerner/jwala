package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Sharvari Barve on 7/6/2017.
 */
public class SortRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the column header with the label \"(.*)\"$")
    public void clickColumnHeader(final String label) {
        jwalaUi.clickWhenReady(By.xpath("//th//*[contains(text(), '" + label + "')]"));
    }

    @Then("^I see first item \"(.*)\"$")
    public void checkFirstElement(final String expectedElementName) {
        List<WebElement> listOfWebElements = jwalaUi.getWebDriver().findElements(By.xpath("//td[button[@class='button-link']]"));
        WebElement firstElement = listOfWebElements.get(0);
        assertEquals(expectedElementName, firstElement.getText());
    }
}