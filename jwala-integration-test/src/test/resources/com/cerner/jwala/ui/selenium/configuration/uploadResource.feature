Feature: Upload a Resource

Scenario: Upload a JVM Resource

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92     |
      | mediaType       | JDK             |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | remoteDir       |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache-tomcat-7.0.55.zip |
      | remoteDir       | remoteDir                |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1            |
      | portNumber | 80                   |
      | group      | seleniumGroup        |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello.xml"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "setenv.bat"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "server.xml"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "server.xml.tpl"
    When I click the upload resource dialog ok button
    Then check resource uploaded successful


Scenario: Upload a Web Server Resource

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:/ctp                  |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver     |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup         |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the "Deploy Path" field with "httpd.resource.deploy.path"
    And I choose the resource file "httpdconf.tpl"
    When I click the upload resource dialog ok button
    Then check resource uploaded successful


Scenario: Upload a Web Application Resource

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello       |
      | group       | seleniumGroup  |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "resource.deploy.path"
    And I choose the resource file "hello-world.war"
    When I click the upload resource dialog ok button
    Then check resource uploaded successful