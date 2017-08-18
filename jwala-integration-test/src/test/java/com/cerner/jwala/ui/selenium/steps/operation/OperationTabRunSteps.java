package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/14/2017
 */
public class OperationTabRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the Operations tab$")
    public void goToOperationsTab() {
        jwalaUi.clickTab("Operations");
    }

    @And("^I expand the group operation's \"(.*)\" group$")
    public void expandGroup(final String groupName) {
        // check first if the group is already expanded, if it is there is no need to expand it
        jwalaUi.click(By.xpath("//td[text()='" + groupName + "']/preceding-sibling::td"));
    }
}
