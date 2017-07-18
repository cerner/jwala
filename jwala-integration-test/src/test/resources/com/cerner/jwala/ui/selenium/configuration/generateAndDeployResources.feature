Feature: Upload Resource - Jvm Node

  Scenario:Jvm Node
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92     |
      | mediaType       | JDK             |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | d:/ctp          |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache-tomcat-7.0.55.zip |
      | remoteDir       | d:/ctp                   |
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:/ctp                  |
    And I created a jvm with following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | USMLVV2CTO0766       |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a webserver with following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | USMLVV2CTO0766      |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |

    And I created a webapp with following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
#    upload webserver resources
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the "Deploy Path" field with "d:\ctp\app\data\httpd\seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful
#    upload jvm resources
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
#    And I fill in the "Deploy Name" field with "hello.xml"
#    And I fill in the "Deploy Path" field with "C:\ctp\app\instances"
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful
    And I clicked on add resource
#    And I fill in the "Deploy Name" field with "setenv.bat"
#    And I fill in the "Deploy Path" field with "C:\ctp\app\instances"
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful
    And I clicked on add resource
#    And I fill in the "Deploy Name" field with "server.xml"
#    And I fill in the "Deploy Path" field with "C:\ctp\app\instances"
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "D:\ctp\app\webapps"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I am in the Operations tab
    And I expanded group "seleniumGroup"
    And I generate webapp
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I start all jvms
    And I wait for popup string "this"



