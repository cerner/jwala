package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import com.cerner.jwala.ui.selenium.steps.CommonRunSteps;
import cucumber.api.java.After;
import cucumber.api.java.cs.A;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class StatusRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @Autowired
    @Qualifier("parameterProperties")
    Properties paramProp;


    @Then("^I see the load balancer page with app \"(.*)\" and host \"(.*)\" and port \"(.*)\"$")
    public void verfiyLoadBalancerPage(String app, String host, String port) throws IOException {
        String hostName = paramProp.getProperty(host);
        jwalaUi.isElementExists(By.xpath("//td[text()='http://" + hostName + ":" + port + "/" + app + "')"));
        jwalaUi.isElementExists(By.xpath("//h1[text()='Load Balancer Manager for" + hostName + "'"));
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

}
