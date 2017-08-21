Feature: Delete JVM, web server in operations page

  Scenario: Delete started webserver

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | host1               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server resource with the following parameters:
      | group        | seleniumGroup              |
      | webServer    | seleniumWebserver          |
      | deployName   | httpd.conf                 |
      | deployPath   | httpd.resource.deploy.path |
      | templateName | httpdconf.tpl              |
    And I check for resource "httpd.conf"
    And I generated the web servers of group "seleniumGroup"
    And I started web server "seleniumWebserver" of group "seleniumGroup"
    And I see the state of "seleniumWebserver" web server of group "seleniumGroup" is "STARTED"
    When I click the delete button of web server "seleniumWebserver" under group "seleniumGroup" in the operations tab
    And I click the operation's confirm delete web server dialog yes button
    Then I see an error dialog box that tells me to stop the web server "seleniumWebServer"

  Scenario: delete new  jvm
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92      |
      | mediaType       | JDK              |
      | archiveFilename | jdk1.8.0_92.zip  |
      | remoteDir       | media.remote.dir |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache-tomcat-7.0.55.zip |
      | remoteDir       | media.remote.dir         |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    When I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group
    When I click the delete button of JVM "seleniumJvm" under group "seleniumGroup" in the operations tab
    And I click the operation's confirm delete jvm dialog yes button
    Then I don't see an error dialog box that tells me to stop the jvm "seleniumJvm"
    And I see a popup that tells me about the succesful delete for jvm "seleniumJvm" and jwala refresh for operations page
    And I click ok on refresh page popup
    And I verify element "seleniumJvm" is succesfully deleted from group "seleniumGroup"


