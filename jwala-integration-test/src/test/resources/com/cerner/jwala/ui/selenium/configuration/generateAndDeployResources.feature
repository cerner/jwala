Feature: Generate and Deploy Resource
    Uses the Generate Web Servers to deploy a web server
    Uses the Generate JVMs to deploy a JVM
    Uses the Start Web Servers button to start a web server
    Uses the Stop JVMs button to stop a JVM

Scenario: Deploy and Run a Web Application

    Given I logged in

    And I am in the Configuration tab

    # create media
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92     |
      | mediaType       | JDK             |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir|
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache-tomcat-7.0.55.zip |
      | remoteDir       | tomcat.media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |

    # create entities
    And I created a group with the name "seleniumGroup"
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | jvm.http.port        |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | host1               |
      | portNumber         | ws.http.port        |
      | httpsPort          | ws.https.port       |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | ws.status.path      |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    # create resources
    And I am in the resource tab
    And I created a JVM resource with the following parameters:
        | group       | seleniumGroup                   |
        | jvm         | seleniumJvm                     |
        | deployName  | setenv.bat                      |
        | deployPath  | jvm.setenv.resource.deploy.path |
        | templateName| setenv.bat.tpl                  |
    And I created a JVM resource with the following parameters:
        | group       | seleniumGroup                       |
        | jvm         | seleniumJvm                         |
        | deployName  | server.xml                          |
        | deployPath  | jvm.server.xml.resource.deploy.path |
        | templateName| server.xml.tpl                      |
    And I created a web server resource with the following parameters:
        | group       | seleniumGroup              |
        | webServer   | seleniumWebserver          |
        | deployName  | httpd.conf                 |
        | deployPath  | httpd.resource.deploy.path |
        | templateName| httpdconf.tpl              |
    And I created a web app resource with the following parameters:
        | group       | seleniumGroup                       |
        | webApp      | seleniumWebapp                      |
        | deployName  | hello.xml                           |
        | deployPath  | webapp.context.resource.deploy.path |
        | templateName| hello.xml.tpl                       |
    And I created a web app resource with the following parameters:
        | group       | seleniumGroup               |
        | webApp      | seleniumWebapp              |
        | deployName  | hello-world.war             |
        | deployPath  | webapp.resource.deploy.path |
        | templateName| hello-world.war             |

    And I am in the Operations tab
    And I expand the group operation's "seleniumGroup" group

    # do the test
    When I click the generate web application button of "seleniumWebapp" web app under group "seleniumGroup"
    Then I see "seleniumWebapp" web application got deployed successfully

    When I click the "Generate Web Servers" button of group "seleniumGroup"
    Then I see that the web servers were successfully generated for group "seleniumGroup"

    When I click the "Generate JVMs" button of group "seleniumGroup"
    Then I see that the JVMs were successfully generated for group "seleniumGroup"

    When I click "Start Web Servers" button of group "seleniumGroup"
    Then I see the state of "seleniumWebserver" web server of group "seleniumGroup" is "STARTED"

    When I click "Start JVMs" button of group "seleniumGroup"
    Then I see the state of "seleniumJvm" JVM of group "seleniumGroup" is "STARTED"