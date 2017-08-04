package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Jedd Cuison on 7/7/2017
 */
public class CreateJvmRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the jvm tab$")
    public void goToJvmTab() {
        jwalaUi.clickTab("JVM");
    }

    @When("^I click the add jvm button$")
    public void clickAddJvmBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Add']]"));
    }

    @And("^I see the jvm add dialog$")
    public void checkForAddJvmDlg() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add JVM']"));
    }

    @And("^I fill in the \"JVM Name\" field with \"(.*)\"$")
    public void setName(final String name) {
        jwalaUi.sendKeys(By.name("jvmName"), name);
    }

    @And("^I fill in the \"JVM Host Name\" field with \"(.*)\"$")
    public void setHostName(final String hostName) {
        jwalaUi.sendKeys(By.name("hostName"), hostName);
    }

    @And("^I fill in the \"JVM HTTP Port\" field with \"(.*)\"$")
    public void setHttpPort(final String httpPort) {
        jwalaUi.sendKeys(By.name("httpPort"), httpPort);
    }

    @And("^I click the \"JVM status path\" field to auto generate it$")
    public void clickStatusPath() {
        jwalaUi.click(By.name("statusPath"));
    }

    @And("^I select the \"JVM JDK\" version \"(.*)\"$")
    public void selectJdk(final String jdk) {
        jwalaUi.selectItem(By.name("jdkMediaId"), jdk);
    }

    @And("^I select the \"JVM Apache Tomcat\" version \"(.*)\"$")
    public void selectTomcat(final String tomcat) {
        jwalaUi.selectItem(By.name("tomcatMediaId"), tomcat);
    }

    @And("^I associate the JVM to the following groups:$")
    public void setGroups(final List<String> groups) {
        for (final String group: groups) {
            jwalaUi.click(By.xpath("//div[text()='" + group + "']/input"));
        }
    }

    @And("^I click the jvm add dialog ok button$")
    public void clickOkBtn() {
        jwalaUi.click(By.xpath("//span[text()='Ok']"));
    }

    @Then("^I see the following jvm details in the jvm table:$")
    public void validateJvm(final Map<String, String> jvmDetails) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + jvmDetails.get("name") + "']"));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + jvmDetails.get("host") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//span[text()='" + jvmDetails.get("group") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//span[text()='" + jvmDetails.get("statusPath") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + jvmDetails.get("http") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + jvmDetails.get("https") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//span[text()='" + jvmDetails.get("jdk") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//span[text()='" + jvmDetails.get("tomcat") + "']")));
    }

    @Then("^I wait for the jvm \"(.*)\"$")
    public void waitForJvm(String name) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + name + "']"));
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
