package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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

    @When("^I click the button for the status of web-server \"(.*)\" in the group \"(.*)\"$")
    public void clickStatusOfWebserver(String webserverName, String groupName){
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webserverName + "']/following-sibling::td//button[text()='status']"));
    }

    @Then("^I see the load balancer page with app \"(.*)\" and host \"(.*)\" and port \"(.*)\"$")
    public void verfiyLoadBalancerPage(String app, String host, String port) throws IOException {
        String hostName = paramProp.getProperty(host);
        jwalaUi.isElementExists(By.xpath("//td[text()='http://" + hostName + ":" + port + "/" + app + "')"));
        jwalaUi.isElementExists(By.xpath("//h1[text()='Load Balancer Manager for" + hostName + "'"));
    }
}
