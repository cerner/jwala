package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/18/2017
 */
public class NavigationRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the Configuration tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Configuration");
    }

    @Given("^I am in the Web Apps tab$")
    public void goToWebAppsTab() {
        jwalaUi.clickTab("Web Apps");
    }

    @Given("^I am in the resource tab$")
    public void goToResourceTab() {
        jwalaUi.clickTab("Resources");
    }

    @Given("^I am in the media tab$")
    public void goToMediaTab() {
        jwalaUi.clickTab("Media");
    }

    @Given("^I am in the Operations tab$")
    public void goToOperationsTab() {
        jwalaUi.clickTab("Operations");
    }

    @Given("^I expand the group operation's \"(.*)\" group$")
    public void expandGroupInOperationsTab(final String groupName) {
        // check first if the group is already expanded, if it is there is no need to expand it
        if (jwalaUi.isElementExists(
                By.xpath("//td[text()='" + groupName + "']/preceding-sibling::td//span[contains(@class,'ui-icon-triangle-1-e')]"))) {
            jwalaUi.click(By.xpath("//td[text()='" + groupName + "']/preceding-sibling::td"));
        }
    }

    /**
     * A generic ok button click to close a message box
     */
    @Given("^I click the ok button$")
    public void clickOkBtn() {
        jwalaUi.click(By.xpath("//button/span[text()='Ok']"));
    }

    @Given("^I am in the admin tab$")
    public void goToAdminTab() {
        jwalaUi.clickTab("Admin");
    }
}
