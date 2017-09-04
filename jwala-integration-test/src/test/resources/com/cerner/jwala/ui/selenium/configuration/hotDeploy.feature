Feature: HotDeploy a resource file


  Scenario:  Deploy an individual JVM resource without hotDeploy
    Given I logged in
    And I am in the Configuration tab
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
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |
    And I generate and start the jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    And I go to the file in resources with the following parameters:
      | componentName | seleniumJvm   |
      | componentType | JVMs          |
      | fileName      | server.xml    |
      | group         | seleniumGroup |

    When I right click resource file "server.xml"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify deploy error message for file "server.xml" and jvm "seleniumJvm"
    And I click ok to resource deploy error message
    Given I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group
    When I generate "seleniumJvm" JVM of "seleniumGroup" group
    Then I verify deploy error message for jvm "seleniumJvm" in operations

  Scenario: Hot Deploy an individual web-server resource
    Given I logged in
    And I am in the Configuration tab
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
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |
    And I generate and start the jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumJvm   |
      | componentType  | JVMs          |
      | fileName       | server.xml    |
      | group          | seleniumGroup |
      | attributeKey   | "hotDeploy"   |
      | attributeValue | true          |
    When I right click resource file "server.xml"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify successful deploy

  Scenario:  Deploy an individual web-server resource without hotDeploy
    Given I logged in
    And I am in the Configuration tab
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
    And I generate and start the webserver with the following parameters:
      | webserverName | seleniumWebserver |
      | group         | seleniumGroup     |
    And I go to the file in resources with the following parameters:
      | componentName | seleniumWebserver |
      | componentType | Web Servers       |
      | fileName      | httpd.conf        |
      | group         | seleniumGroup     |

    When I right click resource file "httpd.conf"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify deploy error message for ws file "httpd.conf" for webserver "seleniumWebserver"
    And I click ok to resource deploy error message
    Given I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group
    When I generate "seleniumWebserver" web server of "seleniumGroup" group
    Then I verify deploy error message for webserver "seleniumWebserver" in operations

  Scenario: Hot Deploy an individual web-server resource
    Given I logged in
    And I am in the Configuration tab
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
    And I generate and start the webserver with the following parameters:
      | webserverName | seleniumWebserver |
      | group         | seleniumGroup     |
    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebserver |
      | componentType  | Web Servers       |
      | fileName       | httpd.conf        |
      | group          | seleniumGroup     |
      | attributeKey   | "hotDeploy"       |
      | attributeValue | true              |
    When I right click resource file "httpd.conf"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify successful deploy