package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class StatusRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    Properties paramProp;

    @Then("^I see the load balancer page with app \"(.*)\" and host \"(.*)\" and port \"(.*)\"$")
    public void verfiyLoadBalancerPage(String app, String host, String port) throws IOException {
        String hostName = paramProp.getProperty(host);
        jwalaUi.isElementExists(By.xpath("//td[text()='http://" + hostName + ":" + port + "/" + app + "')"));
        jwalaUi.isElementExists(By.xpath("//h1[text()='Load Balancer Manager for" + hostName + "'"));
    }
}
