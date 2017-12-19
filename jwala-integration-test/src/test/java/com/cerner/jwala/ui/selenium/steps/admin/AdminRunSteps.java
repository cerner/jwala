package com.cerner.jwala.ui.selenium.steps.admin;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

/**
 * Created by Sharvari Barve on 7/21/2017.
 */
public class AdminRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @When("^I fill in the \"data to be secured\" field with \"(.*)\"$")
    public void fillDataToEncrypt(String data) {
        jwalaUi.sendKeys(By.xpath("//input[@class='toEncrypt']"), data);
    }

    @And("^I click the admin's tab \"(.*)\" button$")
    public void clickEncryptBtn(final String btnLabel) {
        jwalaUi.click(By.xpath("//button/span[text() = '" + btnLabel + "']"));
    }

    @Then("^I see the \"Encryption Succeeded\" message$")
    public void verifyText() {
        jwalaUi.find(By.xpath("//h4[text()='Encryption Succeeded']"));
    }

    @And("^I see the \"(.*)\" heading$")
    public void verifyHeader(final String headerCaption) {
        jwalaUi.find(By.xpath("//h3[text()='" + headerCaption + "']"));
    }

    @And("^I see \"(.*)\" in the \"(.*)\" text box$")
    public void verifyTextAreaItem(final String itemName, final String textAreaHeading) {
        final WebElement webElement = jwalaUi.find(By.xpath("//h3[text()='" + textAreaHeading + "']/following-sibling::p//textArea"));
        final String val = webElement.getAttribute("value");
        assert StringUtils.isNotEmpty(val) && val.contains(itemName);
    }

    @And("I see in the web banner that the client details = \"(.*)\" and data mode = \"(.*)\"")
    public void verifyClientDetails(final String clientDetails, final String dataMode){
        jwalaUi.find(By.xpath("//span[text() = '" + paramProp.getProperty(clientDetails) + "']"));
        jwalaUi.find(By.xpath("//span[text() = '" + paramProp.getProperty(dataMode) + "']"));
    }
}
