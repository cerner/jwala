package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class ThreadDumpRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    private String origWindowHandle;

    @When("^I click thread dump of jvm \"(.*)\" of the group \"(.*)\"$")
    public void clickThreadDump(String jvmName, String groupName){
        origWindowHandle = jwalaUi.getWebDriver().getWindowHandle();
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//button[@title='Thread Dump']"));
    }

    @Then("^I see the thread dump page$")
    public void verifyThreadDumpPage() {
        jwalaUi.switchToOtherTab(origWindowHandle);
        jwalaUi.waitUntilElementIsVisible(By.xpath("//pre[contains(text(), 'Full thread dump')]"));
        if (origWindowHandle != null) {
            jwalaUi.getWebDriver().close();
            jwalaUi.getWebDriver().switchTo().window(origWindowHandle);
        }
    }
}
