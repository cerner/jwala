package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by RS045609 on 7/7/2017.
 */
public class ManageResourceRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the resource tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Resources");
    }

    @And("expand group")
    public void expandGroup() {
        jwalaUi.clickTreeItemExpandCollapseIcon("Rahul group");
    }

    @And("expand webservers")
    public void expandWebserver() {
        jwalaUi.clickTreeItemExpandCollapseIcon("Web Servers");
    }

    @And("click on webserver")
    public void clickWebserver() {
        jwalaUi.clickComponentForUpload("Rahul webserver");
        jwalaUi.sleep();
    }

    @And("click on add resource")
    public void addResource(){
        jwalaUi.clickAddResource();
    }

}
