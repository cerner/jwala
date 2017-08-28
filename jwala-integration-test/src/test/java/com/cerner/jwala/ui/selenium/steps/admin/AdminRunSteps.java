package com.cerner.jwala.ui.selenium.steps.admin;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Sharvari Barve on 7/21/2017.
 */
public class AdminRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Given("^I am in the admin tab$")
    public void goToAdminTab() {
        jwalaUi.clickTab("Admin");
    }

    @When("^I fill in the data to be encrypted \"(.*)\"$")
    public void fillDataToEncrypt(String data) {
        jwalaUi.sendKeys(By.xpath("//input[@class='toEncrypt']"), data);
    }

    @And("^I click \"(.*)\" button$")
    public void clickEncryptButton(String componentName) {
        jwalaUi.click(By.xpath("//button/span[contains(text(),'" + componentName + "')]"));
    }

    @Then("^I see the text \"(.*)\" in properties management box$")
    public void verifyPropertiesManagementTextBox(String text) {
        WebElement element = jwalaUi.getWebElement(By.xpath("//p[8]/textarea"));
        String textInTheBox = element.getAttribute("value");
        assertNotNull(textInTheBox);
        assert textInTheBox.contains(text);
    }

    @Then("^I see the text \"(.*)\" in manifest box$")
    public void verifyManifestTextBox(String text) {
        WebElement element = jwalaUi.getWebElement(By.xpath("//p[10]/textarea"));
        String textInTheBox = element.getAttribute("value");
        assertNotNull(textInTheBox);
        assert textInTheBox.contains(text);
    }

    @And("^I see the implementation version \"(.*)\" in manifest box$")
    public void verifyVersion(String text) {
        WebElement element = jwalaUi.getWebElement(By.xpath("//p[10]/textarea"));
        String textInTheBox = element.getAttribute("value");
        assertNotNull(textInTheBox);
        assert textInTheBox.contains(paramProp.getProperty(text));
    }

    @And("^I verify header with text \"(.*)\"$")
    public void verifyHeader(String text) {
        WebElement header = jwalaUi.getWebElement(By.xpath("//h3[contains(text(),'" + text + "')]"));
        assertNotNull(header);
    }

    @And("^I verify succesful Encryption message$")
    public void verifyText() {
        jwalaUi.isElementExists(By.xpath("//p[contains(text(),'Encryption Succeeded']"));
    }
}
