package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Rahul Sayini on 7/11/2017.
 */
public class OperationRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the Operations tab$")
    public void goToOperationsTab() {
        jwalaUi.clickTab("Operations");
    }

    @And("I generate all webservers")
    public void generateAllWebservers() {
        jwalaUi.click(By.xpath("//button[span[text()='Generate Web Servers']]"));
    }

    @And("I generate all jvms")
    public void generateAllJvms() {
        jwalaUi.click(By.xpath("//button[span[text()='Generate JVMs']]"));
    }

    @And("I start all webservers")
    public void startAllWebServers() {
        jwalaUi.click(By.xpath("//button[span[text()='Start Web Servers']]"));
    }

    @And("I start all jvms")
    public void startAllJvms() {
        jwalaUi.click(By.xpath("//button[span[text()='Start JVMs']]"));
    }
}
