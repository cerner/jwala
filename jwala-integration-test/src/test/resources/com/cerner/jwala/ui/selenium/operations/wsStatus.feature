Feature: Web Server Status

Scenario: Check the status of a started web server

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
        | portNumber | 9000                 |
        | group      | seleniumGroup        |
    And I created a web server with the following parameters:
        | webserverName      | seleniumWebserver   |
        | hostName           | host1               |
        | portNumber         | 80                  |
        | httpsPort          | 443                 |
        | group              | seleniumGroup       |
        | apacheHttpdMediaId | apache-httpd-2.4.20 |
        | statusPath         | /apache_pb.png      |
    And I created a web app with the following parameters:
        | webappName  | seleniumWebapp |
        | contextPath | /hello         |
        | group       | seleniumGroup  |

    # create resources
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

    # generate
    And I generated "seleniumWebapp" web app of "seleniumGroup" group
    And I generated the web servers of "seleniumGroup" group
    And I generated the JVMs of "seleniumGroup" group

    # start
    And I started "seleniumWebserver" web server of "seleniumGroup" group
    And I started "seleniumJvm" JVM of "seleniumGroup" group

    # do the test
    When I click the "status" link of web server "seleniumWebserver" under group "seleniumGroup" in the operations tab
    Then I see the load balancer page with web app "seleniumWebapp" and host "host1" and port "9000"