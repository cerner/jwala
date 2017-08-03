Feature:HealthCheck

  Scenario:Healthcheck
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
    And I created a jvm with the following parameters:
      | jvmName       | seleniumJvm          |
      | tomcatMediaId | apache-tomcat-7.0.55 |
      | jdkMediaId    | jdk1.8.0_92          |
      | hostName      | localhost            |
      | httpPort      | 9000                 |
      | group         | seleniumGroup        |
    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |

    And I created a web app with the following parameters:
      | name        | Healthcheck   |
      | contextPath | /hello        |
      | group       | seleniumGroup |

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
    And I fill in the "Deploy Name" field with "context.xml"
    And I fill in the "Deploy Path" field with "d:\ctp\app\instances\seleniumJvm\apache-tomcat-7.055\conf"
    And I choose the resource file "context.xml"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "setenv.sh.json"
    And I choose the resource file "setenv.sh.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

#    And I fill in the "Deploy Name" field with "setenv.bat"
#    And I fill in the "Deploy Path" field with "C:\ctp\app\instances"
    And I clicked on add resource
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


    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "CatalinaProperties.json"
    And I choose the resource file "CatalinaProperties.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "LoggingProperties.json"
    And I choose the resource file "LoggingProperties.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful


    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "WebXml.json"
    And I choose the resource file "WebXml.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful






    And I expanded component "Web Apps"
    And I clicked on component "Healthcheck"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "healthcheck-webapp-1.0.2.war"
    And I fill in the "Deploy Path" field with "D:\ctp\app\webapps"
    And I choose the resource file "healthcheck-webapp-1.0.2.war"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

   # And I clicked on add resource
   # And I check Upload Meta Data File
  #  And I choose the meta data file "hctProperties.json"
  #  And I choose the resource file "hctProperties.tpl"
  #  And I click the upload resource dialog ok button
   # Then check resource uploaded successful


    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hctProperties.json"
    And I choose the resource file "hctProperties.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hctRoleMappingProperties.json"
    And I choose the resource file "hctRoleMappingProperties.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "propertySourcePropertiesTemplate.json"
    And I choose the resource file "propertySourcePropertiesTemplate.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful


    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "stp.properties.json"
    And I choose the resource file "stp.properties.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

    #And I clicked on add resource
    ##And I fill in the "Deploy Name" field with "ctp_platform_tc7-1.2.19.zip"
    #And I fill in the "Deploy Path" field with "D:/ctp/app/lib"
   # And I choose the resource file "ctp_platform_tc7-1.2.19.zip"
    #And I click the upload resource dialog ok button
   # Then check resource uploaded successful

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "Healthcheck"
    And I wait for popup string "Healthcheck resource files deployed successfully"
    And I click on ok button
    And I generate all webservers
    And I wait for popup string "Successfully generated the web servers for seleniumGroup"
    And I click on ok button
    And I start all webservers
    And I generate all jvms
    And I wait for popup string "Successfully generated the JVMs for seleniumGroup"
    And I click on ok button
    And I start all jvms
    And I wait for component "seleniumWebserver" state "STARTED"
    And I choose the row of the component with name "seleniumWebserver" and click button "Manager"