Feature: HttpdConf

  Scenario: http.conf of a new web-server
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHttpdConf1 |
      | hostName           | host1                       |
      | portNumber         | 80                          |
      | httpsPort          | 443                         |
      | group              | seleniumGroup               |
      | apacheHttpdMediaId | apache-httpd-2.4.20         |
      | statusPath         | /apache_pb.png              |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHttpdConf1"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHttpdConf1"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then check resource "httpd.conf"
    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumWebserverHttpdConf1" and click text "httpd.conf"
    Then I see error while reading conf file

  Scenario:Httpd.Conf a started web-server
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
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvmHttpdConf2 |
      | tomcat     | apache-tomcat-7.0.55  |
      | jdk        | jdk1.8.0_92           |
      | hostName   | host1                 |
      | portNumber | 9000                  |
      | group      | seleniumGroup         |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHttpdConf2 |
      | hostName           | host1                       |
      | portNumber         | 80                          |
      | httpsPort          | 443                         |
      | group              | seleniumGroup               |
      | apacheHttpdMediaId | apache-httpd-2.4.20         |
      | statusPath         | /apache_pb.png              |

    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHttpdConf2"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpdDeployPath" for web server "seleniumWebserverHttpdConf2"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then check resource "httpd.conf"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvmHttpdConf2"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then check resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then check resource "setenv.bat"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then check resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then check resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all jvms
    And I wait for component "seleniumJvm" state "STARTED"
    And I choose the row of the component with name "seleniumWebserver" and click text "httpd.conf"
    Then I see the proper httpd.conf
