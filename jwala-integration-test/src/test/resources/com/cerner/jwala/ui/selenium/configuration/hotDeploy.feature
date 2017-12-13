Feature: HotDeploy a resource file

  Scenario: Hot Deploy an individual web-server resource
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | host1               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache.httpd.media |
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

    When I attempt to deploy the resource "httpd.conf"
    Then I confirm deploy error message popup for ws file "httpd.conf" for webserver "seleniumWebserver"

    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebserver |
      | componentType  | Web Servers       |
      | fileName       | httpd.conf        |
      | group          | seleniumGroup     |
      | attributeKey   | "hotDeploy"       |
      | attributeValue | true              |
      | override       | false             |
    And I wait for notification "Saved"
    When I attempt to deploy the resource "httpd.conf"
    Then I confirm successful deploy popup


  Scenario: Deploy webapp/Jvm-resource
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk.media      |
      | mediaType       | JDK              |
      | archiveFilename | jdk.media.archive  |
      | remoteDir       | media.remote.dir |
    And I created a media with the following parameters:
      | mediaName       | apache.tomcat.media     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | media.remote.dir         |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache.tomcat.media |
      | jdk        | jdk.media          |
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
    And I created a group JVM resource with the following parameters:
      | group        | seleniumGroup                   |
      | deployName   | setenv.bat                      |
      | deployPath   | jvm.setenv.resource.deploy.path |
      | templateName | setenv.bat.tpl                  |
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
    When I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello.xml  |
      | deployOption | individual |
    Then I confirm error message popup for group "seleniumGroup" for jvm file "hello.xml" with one of JVMs as "seleniumJvm"
    And I go to the file in resources with the following parameters:
      | componentName | seleniumJvm   |
      | componentType | JVMs          |
      | fileName      | server.xml    |
      | group         | seleniumGroup |

   #test jvm deploy-resources-error
    When I attempt to deploy the resource "server.xml"
    Then I confirm deploy error message popup for file "server.xml" and jvm "seleniumJvm"

    #test webapp-error -operations
    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I confirm webapp generate error popup in operations for jvm "seleniumJvm"

    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebapp |
      | componentType  | Web Apps       |
      | fileName       | hello.xml      |
      | group          | seleniumGroup  |
      | attributeKey   | "hotDeploy"    |
      | attributeValue | true           |
      | override       | true           |



    # test for successful deployment to a host
    When I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello.xml  |
      | deployOption | individual |
    Then I confirm successful deploy popup

    # test for successful deployment to all hosts
    When I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello.xml |
      | deployOption | all       |
    Then I confirm successful deploy popup

    # test successful deployment of a web app via the operations tab
    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I confirm webapp "seleniumWebapp" is successfully deployed in Operations page popup

    #test web app-resource under Jvm
    And I go to the web-app file in resources under individual jvm with the following parameters:
      | app     | seleniumWebapp |
      | jvmName | seleniumJvm    |
      | group   | seleniumGroup  |
      | file    | hello.xml      |

    When I attempt to deploy the resource "hello.xml"
    Then I confirm successful deploy popup

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

    # test for successful deployment of jvm a resource
    When I attempt to deploy the resource "server.xml"
    Then I confirm successful deploy popup

    #test deploy group jvm resource
    And I enter attribute in the group file MetaData with the following parameters:
      | componentType  | JVMs          |
      | fileName       | setenv.bat    |
      | group          | seleniumGroup |
      | attributeKey   | "hotDeploy"   |
      | attributeValue | true          |
      | override       | true          |

    When I attempt to deploy the jvm group resource "setenv.bat"
    Then I confirm successful deploy popup
