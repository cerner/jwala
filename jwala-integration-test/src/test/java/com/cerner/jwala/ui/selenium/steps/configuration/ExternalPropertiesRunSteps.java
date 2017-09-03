package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

/**
 * Created by SB053052 on 9/2/2017.
 */
public class ExternalPropertiesRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Then("^I verify external property override message$")
    public void verifyOverrideMsg() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Only one external properties file can be uploaded. Any existing ones will be overwritten.']"));
    }

}
