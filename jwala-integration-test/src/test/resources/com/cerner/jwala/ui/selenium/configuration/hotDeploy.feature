Feature: HotDeploy a resource file

  Scenario:  Deploy an individual JVM resource
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

   #test jvm deploy-resources-error
    When I right click resource file "server.xml"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify deploy error message for file "server.xml" and jvm "seleniumJvm"

    And I go to the file in resources with the following parameters:
      | componentName | seleniumJvm   |
      | componentType | JVMs          |
      | fileName      | server.xml    |
      | group         | seleniumGroup |
    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumJvm   |
      | componentType  | JVMs          |
      | fileName       | server.xml    |
      | group          | seleniumGroup |
      | attributeKey   | "hotDeploy"   |
      | attributeValue | true          |
      | override       | false         |
    And I wait for notification "Saved"

   #test deploy-resources jvm-succesful
    When I right click resource file "server.xml"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify and confirm successful deploy

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
    And I go to the file in resources with the following parameters:
      | componentName | seleniumWebserver |
      | componentType | Web Servers       |
      | fileName      | httpd.conf        |
      | group         | seleniumGroup     |

    When I right click resource file "httpd.conf"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify deploy error message for ws file "httpd.conf" for webserver "seleniumWebserver"

    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebserver |
      | componentType  | Web Servers       |
      | fileName       | httpd.conf        |
      | group          | seleniumGroup     |
      | attributeKey   | "hotDeploy"       |
      | attributeValue | true              |
      | override       | false             |
    And I wait for notification "Saved"
    When I right click resource file "httpd.conf"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    Then I verify and confirm successful deploy


  Scenario: Deploy webapp-resource
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |
    And I created a web app resource with the following parameters:
      | group        | seleniumGroup                       |
      | webApp       | seleniumWebapp                      |
      | deployName   | hello.xml                           |
      | deployPath   | webapp.context.resource.deploy.path |
      | templateName | hello.xml.tpl                       |
    And I generate and start the jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    And I go to the file in resources with the following parameters:
      | componentName | seleniumWebapp |
      | componentType | Web Apps       |
      | group         | seleniumGroup  |
      | fileName      | hello.xml      |
    When I right click resource file "hello.xml"
    And I click resource deploy option
    And I click resource deploy to a host option
    And I confirm the resource deploy to a host popup
    Then I verify error message for group "seleniumGroup" for app file "hello.xml" with one of JVMs as "seleniumJvm"
    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I verify webapp generate error in operations for jvm "seleniumJvm"
    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebapp |
      | componentType  | Web Apps       |
      | fileName       | hello.xml      |
      | group          | seleniumGroup  |
      | attributeKey   | "hotDeploy"    |
      | attributeValue | true           |
      | override       | true           |



    #Test deploy to a host-succesful
    When I right click resource file "hello.xml"
    And I click resource deploy option
    And I click resource deploy to a host option
    And I confirm the resource deploy to a host popup
    Then I verify and confirm successful deploy

    #Test deploy to all host-succesful
    When I right click resource file "hello.xml"
    And I click resource deploy option
    And I click resource deploy option
    And I click resource deploy All option
    And I click yes button to deploy a resource popup
    Then I verify and confirm successful deploy

    #Test operations webapp deploy-succesful
    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I verify and confirm that webapp "seleniumWebapp" is succesfully deployed in Operations page
