# Overview

This folder contains the files required to run the Cucumber-Selenium tests. The recommended way of using it is to copy
jwala-ui-integ-test-support-files folder to a drive e.g. drive c or d and download the required drivers and media files then
run the UI integration test using the following command.
```
$ gradle clean test -Dtest.single=JwalaUiMainTest -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=d:/jwala-ui-integ-test-support-files/drivers/chromedriver.exe -Dtest.property.path=d:/jwala-ui-integ-test-support-files/properties/test-localhost.properties -PjwalaIntegrationTest
```