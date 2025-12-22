package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jedd Cuison on 7/5/2017
 */
public class CreateWebAppRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the add web app button$")
    public void clickAddWebAppBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Add']]"));
    }
    
    @When("^I click the delete web app button$")
    public void clickDeleteWebAppBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Delete']]"));
    }

    @And("^I see the web app add dialog$")
    public void checkForWebAppDialog() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add Web Application']"));
    }

    @And("^I fill in the web app \"Name\" field with \"(.*)\"$")
    public void setWebAppName(final String name) {
        jwalaUi.sendKeys(By.name("name"), name);
    }

    @And("^I fill in the web app \"Context Path\" field with \"(.*)\"$")
    public void setContextPath(final String contextPath) {
        jwalaUi.sendKeys(By.name("webappContext"), contextPath);
    }

    @And("^I associate the web app to the following groups:$")
    public void setGroups(final List<String> groups) {
        for (final String group: groups) {
            jwalaUi.click(By.xpath("//div[text()='" + group + "']/input"));
        }
    }

    @And("^I click the Secure checkbox$")
    public void changeSecureFlag() {
        jwalaUi.click(By.name("secure"));
    }

    @And("^I click the Unpack WAR checkbox$")
    public void changeUnpackWarFlag() {
        jwalaUi.click(By.name("unpackWar"));
    }

    @And("^I click the add web app dialog ok button$")
    public void clickAddDialogOkBtn() {
        jwalaUi.clickOkWithSpan();
    }

    @And("^I see the following web app details in the web app table:$")
    public void checkForWebApp(final Map<String, String> webAppDetails) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + webAppDetails.get("name") + "']"));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + webAppDetails.get("context") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + webAppDetails.get("group") + "']")));
    }

    @And("^I see \"(.*)\" web app table$")
    public void checkForWebApp(final String webAppName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + webAppName + "']"));
    }

    @And("^I click on \"(.*)\" link to open Edit Web App dialog$")
    public void openWebAppEditDialog(final String webAppName) {
        jwalaUi.click(By.xpath("//button[text()='" + webAppName + "']"));
    }

    @And("^I see Unpack WAR checkbox is \"(.*)\"$")
    public void checkUnpackWarFlag(final String flag) {
        if (flag.equalsIgnoreCase("checked")){
            assertTrue(jwalaUi.isCheckBoxChecked(By.name("unpackWar")));
        } else if (flag.equalsIgnoreCase("unchecked")) {
            assertFalse(jwalaUi.isCheckBoxChecked(By.name("unpackWar")));
        }
    }

    @And("^I see Secure checkbox is \"(.*)\"$")
    public void checkSecureFlag(final String flag) {
        if (flag.equalsIgnoreCase("checked")){
            assertTrue(jwalaUi.isCheckBoxChecked(By.name("secure")));
        } else if (flag.equalsIgnoreCase("unchecked")) {
            assertFalse(jwalaUi.isCheckBoxChecked(By.name("secure")));
        }
    }
    
    @And("^I select the \"(.*)\" application row in the table$")
    public void selectGroup(final String appName) {
        jwalaUi.selectTableRowWithContent(appName);
    }
    
    @And("^I see the web app delete dialog$")
    public void checkForDeleteWebAppDialog() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Delete Web Application']"));
    }
    
    @And("^I click the delete web app dialog Yes button$")
    public void clickDeleteDialogYesBtn() {
        jwalaUi.clickYesWithSpan();
    }
    
    @And("^I see the \"The table is empty!\" message$")
    public void checkForEmptyTable() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(@class,'noDataFoundMsg') and contains(text(),'The table is empty!')]"));
    }
}
