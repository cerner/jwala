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
 * Created by SB053052 on 7/6/2017.
 */
public class ManageSortRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @When("I click on the sort button of \"(.*)\"$")
    public void clickSortButton(String tab) {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(text(), '" + tab + "Name')]"));
    }


    @When("I click on the sort button of web server with attribute \"(.*)\"$")
    public void clickSortButtonOfWebServer(String attribute) {
        jwalaUi.click(By.xpath("//div[text()='" + attribute + "']/.."));
    }


    @Then("^I see first item \"(.*)\"$")
    public void checkFirstElement(String name) {
        List<WebElement> listOfWebElements = jwalaUi.getWebDriver().findElements(By.xpath("//td[button[@class='button-link']]"));
        WebElement firstElement = listOfWebElements.get(0);
        assertEquals(firstElement.getText(), name + "");
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

}
