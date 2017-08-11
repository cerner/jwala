package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Sharvari Barve on 8/10/2017.
 */
public class HotDeployRunSteps {
    @Autowired
    JwalaUi jwalaUi;

    @When("I click \"(.*)\" tab$")
    public void clickTab(String text) {
        jwalaUi.click(By.xpath("//a[text()='" + text + "']"));
    }


    @When("I select resource file \"(.*)\"$")
    public void selectFile(String fileName) {
        jwalaUi.click(By.xpath("//span[text()='" + fileName + "']"));
    }

    @When("I right click resource file \"(.*)\"$")
    public void rightClickFile(String fileName) {
        jwalaUi.rightClick(By.xpath("//span[text()='" + fileName + "']"));
    }

    @When("I click deploy option$")
    public void clickDeploy(){
        jwalaUi.click(By.xpath("//*[text()='deploy']"));
    }

    @When("I click deploy to a host option$")
    public void clickDeployToAHost(){
        jwalaUi.click(By.xpath("//*[text()='a host']"));
    }

    @When("I click deploy All option$")
    public void clickDeployAll(){
        jwalaUi.click(By.xpath("//*[text()='all hosts']"));
    }



    @When("^I enter hot Deploy value \"(.*)\" in the edit box for the file \"(.*)\"$")
    public void checkForHotDeploy(String text, String file) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[text()='{']"));

        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(text(),'hotDeploy')]"));
        if (elements.size() > 0) {
            WebElement finalElement = elements.get(0);
        }
        if (elements.size() == 0) {
            jwalaUi.clickWhenReady(By.xpath("//*[text()='{']"));
            jwalaUi.sendKeys(Keys.DELETE);
            jwalaUi.sendKeys("{");
            jwalaUi.sendKeys(Keys.ENTER);
            jwalaUi.sendKeys(text + ",");
        } else {
            WebElement hotDeployElement =jwalaUi.getWebElement(By.xpath("//span[contains(text(), 'hotDeploy')]"));
            hotDeployElement.findElement(By.xpath("..")).findElement(By.xpath("..")).click();
            int i = 5;
            while (i >= 0) {
                jwalaUi.sendKeys(Keys.BACK_SPACE);
                i--;
            }
            String value = "true";
            jwalaUi.sendKeys(value);
            jwalaUi.sendKeys(Keys.ENTER);


        }
    }

    @When("^I enter deployToAllJvms value \"(.*)\" in the edit box for the file \"(.*)\"$")
    public void checkForDeployToAllJvms(String text, String file) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[text()='{']"));

        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(text(),'deployToJvms')]"));
        if (elements.size() > 0) {
            WebElement finalElement = elements.get(0);
        }
            WebElement jvmElement =jwalaUi.getWebElement(By.xpath("//span[contains(text(), 'deployToJvms')]"));
            jvmElement.findElement(By.xpath("..")).findElement(By.xpath("..")).click();
            int i = 5;
            while (i >= 0) {
                jwalaUi.sendKeys(Keys.BACK_SPACE);
                i--;
            }
            String value = "true";
            jwalaUi.sendKeys(value);
            jwalaUi.sendKeys(Keys.ENTER);

    }

    @Then("I verify successful deploy$")
    public void verifySuccesfulDeploy(){
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Deploy successful!')]"));
    }
    @Then("I verify error message for file \"(.*)\" for jvm \"(.*)\"$")
    public void verifyErrorMessageIndividiualJvm(String file, String jvm){
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target JVM "+jvm+" must be stopped or the resource "+file+" must be set to hotDeploy=true before attempting to update the resource files')]"));
    }
}
