Feature: Add, Edit and Delete a JVM

Scenario: Add JVM

    Given I logged in
    And I am in the Configuration tab
    And I created a media with the following parameters:
            |mediaName      |jdk1.8.0_92     |
            |mediaType      |JDK             |
            |archiveFilename|jdk1.8.0_92.zip |
            |remoteDir      |media.remote.dir|
    And I created a media with the following parameters:
            |mediaName      |apache-tomcat-7.0.55    |
            |mediaType      |Apache Tomcat           |
            |archiveFilename|apache-tomcat-7.0.55.zip|
            |remoteDir      |media.remote.dir        |
    And I created a group with the name "GROUP_FOR_ADD_JVM_TEST"
    And I am in the jvm tab
    When I click the add jvm button
    And I see the jvm add dialog
    And I fill in the "JVM Name" field with "JVM_X"
    And I fill in the "JVM Host Name" field with "host1"
    And I fill in the "JVM HTTP Port" field with "9100"
    And I click the "JVM status path" field to auto generate it
    And I select the "JVM JDK" version "jdk1.8.0_92"
    And I select the "JVM Apache Tomcat" version "apache-tomcat-7.0.55"
    And I associate the JVM to the following groups:
        |GROUP_FOR_ADD_JVM_TEST|
    And I click the jvm add dialog ok button
    Then I see the following jvm details in the jvm table:
        |name      |JVM_X                                  |
        |host      |host1                                  |
        |group     |GROUP_FOR_...                          |
        |statusPath|https://host1:91...                    |
        |http      |9100                                   |
        |https     |9101                                   |
        |userName  |                                       |
        |jdk       |jdk1.8.0_92                            |
        |tomcat    |apache-tomcat-7.0.55                   |
