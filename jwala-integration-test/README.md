# Prerequisite Before Running the Tests
- Edit selenium/test.properties by setting the following:
    -   jwala.user.name
    -   jwala.user.pwd
    -   jwala.db.userName
    -   jwala.db.password
- Point the base.url in selenium/test.properties to a running Jwala instance. **It is highly recommended that this instance is local when writing and "testing" tests**.


# How to Run Tests

### in Gradle
Running all tests
```ssh
$ gradle test -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe
```
Running a single test
```ssh
$ gradle test -Dtest.single=ManageGroupTest -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe
```
### in the IDE
In the run configurations set the VM options to
```ssh
-Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe
```
