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

    @Given("^I am in the resource tab$")
    public void goToResourceTab() {
        jwalaUi.clickTab("Resources");
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
}
