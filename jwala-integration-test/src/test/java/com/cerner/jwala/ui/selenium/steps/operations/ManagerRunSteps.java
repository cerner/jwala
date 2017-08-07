package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Created by SB053052 on 7/24/2017.
 */
public class ManagerRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @Autowired
    @Qualifier("parameterProperties")
    Properties paramProp;

    @When("I verify url with port \"(.*)\" and with host \"(.*)\"$")
    public void login(String port, String host) {
        String url = jwalaUi.getWebDriver().getCurrentUrl();
        String hostName = paramProp.getProperty(host);
        assertEquals(url,"https://" + hostName + ":" + port + "/manager/html");
        assert (url.contains("https://" + hostName + ":" + port + "/manager/html"));
    }


}
