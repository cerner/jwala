Feature: Deleting a resource

  Scenario: Delete a resource -individual jvm
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
    Then No resource is present

    #Test group resource delete
    And I created a group JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |
    And I click check-box for resourceFile "server.xml"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then No resource is present
    When I click "seleniumJvm" component
    Then I check for resource "server.xml"


  Scenario: Delete an individual  web-server resource
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
    Then I check for resource "httpd.conf"
    When I click check-box for resourceFile "httpd.conf"
    And I click the resource delete icon
    And I confirm delete a resource popup
    Then No resource is present

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
    Then No resource is present
