Feature: Hot Deploy for a single host

  Scenario: Hot Deploy an individual jvm resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I select resource file "setenv.bat"
    And I click "Meta Data" tab
    And I enter value ""hotDeploy" : "true"" in the edit box for the file "setenv.bat.tpl"
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I right click resource file "setenv.bat"
    And I click deploy option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button


  Scenario:  hot deploy an group jvm resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I select resource file "setenv.bat"
    And I click "Meta Data" tab
    And I enter value ""hotDeploy" : "true"" in the edit box for the file "setenv.bat.tpl"
    And I click save button of "content_Meta_Data"
    And I click on ok button
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I right click resource file "setenv.bat"
    And I click deploy option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button


  Scenario: deploy to a host to an individual web app resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |


    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I select resource file "hello-world.war"
    And I click "Meta Data" tab
    And I enter hot Deploy value ""hotDeploy": true" in the edit box for the file "hello-world.war"
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I right click resource file "hello-world.war"
    And I click deploy option
    And I click deploy to a host option
    And I click on ok button
    And I verify successful deploy
    And I click on ok button


  Scenario: deploy all to an individual web app resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I select resource file "hello-world.war"
    And I click "Meta Data" tab
    And I enter hot Deploy value ""hotDeploy": true" in the edit box for the file "hello-world.war"
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I right click resource file "hello-world.war"
    And I click deploy option
    And I click deploy All option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button

  Scenario: deploy an web app resource from jvm level
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"

    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I clicked on component "seleniumJvm"
    And I expanded component "seleniumJvm"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I expanded component "seleniumJvm"
    And I clicked on component "seleniumWebapp"
    And I select resource file "hello-world.war"
    And I click "Meta Data" tab
    And I enter hot Deploy value ""hotDeploy": true" in the edit box for the file "hello-world.war"
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I expanded component "seleniumJvm"
    And I clicked on component "seleniumWebapp"
    And I right click resource file "hello-world.war"
    And I click deploy option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button


  Scenario: Hot Deploy an individual web-server resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I select resource file "httpd.conf"
    And I click "Meta Data" tab
    And I enter value ""hotDeploy" : "true"" in the edit box for the file "httpdconf.tpl"
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button

  Scenario:  hot deploy an group web-server resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "Web Servers"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "Web Servers"
    And I select resource file "httpd.conf"
    And I click "Meta Data" tab
    And I enter value ""hotDeploy" : "true"" in the edit box for the file "httpdconf.tpl"
    And I click save button of "content_Meta_Data"
    And I click on ok button
    And I wait for "Saved"
    And I am in the Operations tab
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "Web Servers"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify successful deploy
    And I click on ok button


  Scenario: Error in jvm resource without hotDeploy
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I right click resource file "setenv.bat"
    And I click deploy option
    And I click on yes button
    And I verify error message for file "setenv.bat" for jvm "seleniumJvm"


  Scenario: Error in web-server resource without hotDeploy
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify error message for ws file "httpd.conf" for webserver "seleniumWebserverHotDeploy"


  Scenario:  hot deploy error an group jvm resource
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I right click resource file "setenv.bat"
    And I click deploy option
    And I click on yes button
    And I verify error message for group "seleniumGroup" for jvm file "setenv.bat" with one of JVMs as "seleniumJvm"


  Scenario:  hot deploy an group web-server resource Error
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "Web Servers"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "Web Servers"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify ws error message for group "seleniumGroup" for ws file "httpd.conf" with one of Web-servers as "seleniumWebserverHotDeploy"

  Scenario: deploy to a host to an individual web app resource error
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
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserverHotDeploy |
      | hostName           | host1                      |
      | portNumber         | 80                         |
      | httpsPort          | 443                        |
      | group              | seleniumGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20        |
      | statusPath         | /apache_pb.png             |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |


    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserverHotDeploy"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserverHotDeploy"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.bat.json"
    And I choose the resource file "setenv.bat.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "setenv.bat"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "server.xml.json"
    And I choose the resource file "server.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "server.xml"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "webapp.resource.deploy.path"
    And I choose the resource file "hello-world.war"
    And I click the upload resource dialog ok button
    Then I check for resource "hello-world.war"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I wait for popup string "seleniumWebapp resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I start all jvms
    And I wait for component "seleniumWebserverHotDeploy" state "STARTED"
    And I wait for component "seleniumJvm" state "STARTED"
    And I am in the configuration tab
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I right click resource file "hello-world.war"
    And I click deploy option
    And I click deploy to a host option
    And I click on ok button
    And I verify error message for group "seleniumGroup" for app file "hello-world.war" with one of JVMs as "seleniumJvm"