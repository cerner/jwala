package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates run steps related to the control of a particular web server of a certain group and the expected
 * result of such action
 *
 * Created by Jedd Cuison on 8/21/2017
 */
public class WebServerControlRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    Properties paramProp;

    private String origWindowHandle;


    @Autowired
    private static JschService jschService;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private static Properties props;

    @When("^I click the \"(.*)\" button of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickControlWebServerBtn(final String buttonTitle, final String webServerName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[@title='" + buttonTitle + "']"));
    }

    @When("^I click the operation's confirm delete \"(.*)\" web server dialog yes button$")
    public void clickConfirmWebServerDeleteYesButton(final String webServerName) {
        jwalaUi.click(By.xpath("//div[contains(@class, 'ui-dialog') and contains(@style, 'display: block')][div[text()='Are you sure you want to delete web server "
                + webServerName + "']]//button[text()='Yes']"));
    }

    @When("^I click the \"(.*)\" link of web server \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickWebServerControlLink(final String linkLabel, final String webServerName, final String groupName) {
        origWindowHandle = jwalaUi.getWebDriver().getWindowHandle();
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//button[text()='" + linkLabel + "']"));
    }

    @Then("^I see an error dialog box that tells me to stop the web server \"(.*)\"$")
    public void deleteError(final String webServerName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Please stop web server " + webServerName
                + " first before attempting to delete it']"));
    }

    @Then("^I see that \"(.*)\" web server got deleted successfully from the operations tab$")
    public void verifyIfDeleteWebServerIsSuccessful(final String webServerName) {
        jwalaUi.waitUntilElementIsVisible(
                By.xpath("//div[text()='Web server " + webServerName
                        + " was successfully deleted. Jwala will need to refresh to display the latest data and recompute the states.']"));
        SeleniumTestHelper.checkServiceDeleteWasSuccessful(webServerName, jschService, props);
    }

    @Then("^I see the httpd.conf$")
    public void verifyProperConfFile() {
        jwalaUi.switchToOtherTab(origWindowHandle);
        jwalaUi.waitUntilElementIsVisible(By.xpath("//pre[contains(text(),'This is the main Apache HTTP server configuration file.')]"), 60);
        if (origWindowHandle != null) {
            jwalaUi.getWebDriver().close();
            jwalaUi.getWebDriver().switchTo().window(origWindowHandle);
        }
    }

    @Then("^I see the load balancer page$")
    public void verfiyLoadBalancerPage() throws IOException {
        jwalaUi.switchToOtherTab(origWindowHandle);
        jwalaUi.waitUntilElementIsVisible(By.xpath("//h1[contains(text(), 'Load Balancer Manager for')]"), 60);
        if (origWindowHandle != null) {
            jwalaUi.getWebDriver().close();
            jwalaUi.getWebDriver().switchTo().window(origWindowHandle);
        }
    }
}
