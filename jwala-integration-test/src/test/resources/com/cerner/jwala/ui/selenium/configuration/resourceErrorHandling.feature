Feature: deploying a resource template with error produces the appropriate errors

  Scenario: Deploy webapp resource with error
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
    And I created a web app resource with the following parameters:
      | group        | seleniumGroup                       |
      | webApp       | seleniumWebapp                      |
      | deployName   | hello.xml                           |
      | deployPath   | webapp.context.resource.deploy.path |
      | templateName | hello.xml.tpl                       |

    And I enter text in resource edit box and save with the following parameters:
      | fileName | hello.xml |
      | tabLabel | Template  |
      | text     | ${{       |
      | position | Context   |
    And I click the ok button to override JVM Templates
    And I wait for notification "Saved"

    And I right click resource file "hello.xml"
    And I click resource deploy option
    And I click resource deploy to a host option
    And I confirm the resource deploy to a host popup
    And I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

  Scenario: deploy an individual  web-server resource with error-template-from resource and Operations
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

    And I enter text in resource edit box and save with the following parameters:
      | fileName | httpd.conf |
      | tabLabel | Template   |
      | text     | ${{        |
      | position | #          |
    And I wait for notification "Saved"
    And I right click resource file "httpd.conf"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    And I confirm resource deploy error popup for file "httpd.conf" and webserver "seleniumWebserver"
    When I try to generate webserver with the following parameters:
      | webserverName | seleniumWebserver |
      | group         | seleniumGroup     |
    Then I confirm resource deploy error popup for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploying an resource with a garbage value in multiple resources in Jvm
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

    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |


    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml |
      | tabLabel | Template   |
      | text     | ${{        |
      | position | <!--       |
    And I wait for notification "Saved"
    And I wait for the save button to be visible again
    And I enter text in resource edit box and save with the following parameters:
      | fileName | setenv.bat                                                                               |
      | tabLabel | Template                                                                                 |
      | text     | ${{                                                                                      |
      | position | SET CATALINA_HOME=${jvm.tomcatMedia.remoteDir}/${jvm.jvmName}/${jvm.tomcatMedia.rootDir} |
    And I wait for notification "Saved"
    And I right click resource file "setenv.bat"
    And I click resource deploy option
    And I click yes button to deploy a resource popup
    And I confirm resource deploy error popup for file "setenv.bat" and jvm "seleniumJvm"
    When I try to generate jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    Then I confirm multiple resource deploy error popup for file "server.xml" and file "setenv.bat" and jvm "seleniumJvm"


  Scenario: deploying an resource with a garbage value in  jvm resource
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
    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml |
      | tabLabel | Template   |
      | text     | ${{        |
      | position | <!--       |
    And I wait for notification "Saved"
    When I try to generate jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    Then I confirm resource deploy error popup for file "server.xml" and jvm "seleniumJvm"

  Scenario: deploying an resource with a garbage value in individual jvm resource-meta data
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
    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml |
      | tabLabel | Meta Data  |
      | text     | {{         |
      | position | {          |
    And I confirm to unable to save error popup
    And I delete the line in the resource file with the following parameters:
      | fileName | server.xml |
      | tabLabel | Meta Data  |
      | textLine | {{         |
    And I wait for notification "Saved"