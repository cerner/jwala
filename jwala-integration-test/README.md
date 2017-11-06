# Prerequisite Before Running the Tests
- Edit selenium/test.properties by setting the following:
    -   jwala.user.name
    -   jwala.user.pwd
    -   jwala.db.userName
    -   jwala.db.password
- Point the base.url in selenium/test.properties to a running Jwala instance. **It is highly recommended that this instance is local when writing and "testing" tests**.


# How to Run Tests

### in Gradle
Running a test
```ssh
$ gradle clean test -Dtest.single=JwalaUiMainTest -Dwebdriver.class=org.openqa.selenium.chrome.ChromeDriver -Dwebdriver.chrome.driver=d:/jwala-ui-integ-test-support-files/drivers/chromedriver.exe -Dtest.property.path=d:/jwala-ui-integ-test-support-files/properties/test.properties -PjwalaIntegrationTest
```

For IE

```
 gradle clean test -Dtest.single=SecurityTest -Dwebdriver.class=org.openqa.selenium.ie.InternetExplorerDriver -Dwebdriver.ie.driver=d:/jwala-ui-integ-test-support-files/drivers/IEDriverServer32.exe -Dtest.property.path=d:/jwala-ui-integ-test-support-files/properties/test-localhost.properties -PjwalaIntegrationTest
```

# Jwala Db Backup Scripts

These scripts were meant to be used to backup a Jwala instance's before running Selenium tests then restore the db
afterwards

- jwala-backup-db.sh backs up the db to $DB_HOME/jwala.h2.db.bak
- jwala-restore-db.sh copies $DB_HOME/jwala.h2.db.bak back to $DB_HOME/jwala.h2.db

> Note: Please be aware that before editing these scripts that they require Unix\OSX line endings or they will have errors
on execution

# How to Run using IE in Jenkins

1. Have Jwala installed in the machine
2. Install Git (default setup)
3. Create d:\jenkins
4. Copy jenkins.war in d:\jenkins (Please note that it is recommended that Master and Slave should have the same version)
5. Copy JDK in d:\jenkins\java or whatever directory you choose
6. Add the java's bin path to the Path environment
7. Restart the server (slave)
8. Go to Jenkins (Master) and log in
9. Go to Manage Jenkins
10. Click Manage Nodes
11. Click New Node
12. Specify the node name, select permanent agent the click ok
13. Specify d:\jenkins for the root directory
14. Select "Launch slave agents via SSH" for the Launch method
15. Specify the host
16. Specify the credentials
17. Select "Non verifying Verification Strategy" for the Host Key Verification Strategy
18. Click save
19. Go to the newly created agent and click Launch agent
20. Open the configuration of the job which runs cucumber/selenium tests in IE
21. Check "Restrict where this project can be run"
22. Specify the slave's name then click save
23. Copy the jwala-ui-integ-test-support-files directory and its content to the slave (e.g. d:\jwala-ui-integ-test-support-files)
23. Run the job, you should see the job under the slave Jenkins (node)



