Feature: Handle a resource

  Scenario: Delete a resource -individual jvm
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
      | group        | seleniumGroup                   |
      | jvm          | seleniumJvm                     |
      | deployName   | setenv.bat                      |
      | deployPath   | jvm.setenv.resource.deploy.path |
      | templateName | setenv.bat.tpl                  |
    And I click "seleniumJvm" node
    And I select the resource file "setenv.bat"
    And I click check-box for resourceFile "setenv.bat"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then I don't see "setenv.bat"

    #Test group resource delete
    And I created a group JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |
    And I click check-box for resourceFile "server.xml"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then I don't see "server.xml"
    When I click "seleniumJvm" component
    Then I check for resource "server.xml"


  Scenario: Handle an individual  web-server resource(delete a resource and removal of parent group defect)
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
    Then I check for resource "httpd.conf"
    When I click "httpd.conf" node
    And I expand "webServer" node in data tree
    Then I don't see "parentGroup" node in the data tree
    When I click check-box for resourceFile "httpd.conf"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then I don't see "httpd.conf"


  Scenario: Resource webapp delete
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I created a web app resource with the following parameters:
      | group        | seleniumGroup                       |
      | webApp       | seleniumWebapp                      |
      | deployName   | hello.xml                           |
      | deployPath   | webapp.context.resource.deploy.path |
      | templateName | hello.xml.tpl                       |
    When I click "seleniumWebapp" component
    And I click check-box for resourceFile "hello.xml"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then I don't see "hello.xml"

