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
$ gradle test -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe -PjwalaIntegrationTest
```
Running a single test
```ssh
$ gradle test -Dtest.single=ManageGroupTest -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe -PjwalaIntegrationTest
```
### in the IDE
In the run configurations set the VM options to
```ssh
-Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=C:/selenium/chromedriver.exe
```

# Jwala Db Backup Scripts

These scripts were meant to be used to backup a Jwala instance's before running Selenium tests then restore the db
afterwards

- jwala-backup-db.sh backs up the db to $DB_HOME/jwala.h2.db.bak
- jwala-restore-db.sh copies $DB_HOME/jwala.h2.db.bak back to $DB_HOME/jwala.h2.db

> Note: Please be aware that before editing these scripts that they require Unix\OSX line endings or they will have errors
on execution