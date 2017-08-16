Feature: deploying a resource template with error produces the appropriate errors

  Scenario: Resource error in webapp-template
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hctProperties.json"
    And I choose the resource file "hctProperties.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "hct.properties"

    Then I select resource file "hct.properties"
    And I click "Template" tab
    And I enter garbage value in template at text "domain"
    And I click save button of "content_Template"
    And I wait for "Saved"

    And I right click resource file "hct.properties"
    And I click deploy option
    And I click deploy to a host option
    And I click on ok button
    And I verify error for resourceFile "hct.properties" and web app "seleniumWebapp"


  Scenario: Resource error in webapp-template-Operations
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

    And I am in the resource tab
    And I expanded component "seleniumGroup"

    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"

    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hctProperties.json"
    And I choose the resource file "hctProperties.tpl"
    And I click the upload resource dialog ok button
    Then  I check for resource "hct.properties"

    Then I select resource file "hct.properties"
    And I click "Template" tab
    And I enter garbage value in template at text "domain"
    And I click save button of "content_Template"
    And I wait for "Saved"

    And I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I generate webapp "seleniumWebapp"
    And I verify error for resourceFile "hct.properties" and web app "seleniumWebapp"


  Scenario: deploying an resource with a garbage value in group jvm resource-meta data
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Meta Data" tab
    And I enter garbage value in metadata
    When I click save button of "content_Meta_Data"
    Then I verify metaData error
    And I click on ok button
    And I clicked on component "hello.xml"
    And I click "Meta Data" tab
    And I erase garbage value
    And I click save button of "content_Meta_Data"
    And I click on ok button
    And I wait for "Saved"


  Scenario: deploying an resource with a garbage value in individual jvm resource-meta data
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Meta Data" tab
    And I enter garbage value in metadata
    And I click save button of "content_Meta_Data"
    And I verify metaData error
    And I click on ok button
    And I clicked on component "hello.xml"
    And I click "Meta Data" tab
    And I erase garbage value
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"

  Scenario: deploying an resource with a garbage value in individual jvm resource-Template-Operations
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Template" tab
    And I enter garbage value in template at text "<"
    And I click save button of "content_Template"
    And I wait for "Saved"
    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumJvm" and click button "Generate JVM resources files and deploy as a service"
    Then I verify resource deploy error for file "hello.xml" and jvm "seleniumJvm"


  Scenario: deploying an resource with a garbage value in individual jvm resource-Template
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "seleniumJvm"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Template" tab
    And I enter garbage value in template at text "<"
    And I click save button of "content_Template"
    And I wait for "Saved"
    And I clicked on component "hello.xml"
    And I right click resource file "hello.xml"
    And I click deploy option
    And I click on yes button
    And I verify resource deploy error for file "hello.xml" and jvm "seleniumJvm"


  Scenario: deploying an resource with a garbage value in group jvm resource
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Template" tab
    And I enter garbage value in template at text "<"
    And I click save button of "content_Template"
    And I click on ok button
    And I wait for "Saved"
    And I clicked on component "hello.xml"
    And I right click resource file "hello.xml"
    And I click deploy option
    And I click on yes button
    And I verify resource deploy error for file "hello.xml" and jvm "seleniumJvm"


  Scenario: deploying an resource with a garbage value in group jvm resource-Operations
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "JVMs"
    And I clicked on component "JVMs"
    And I clicked on add resource
    And I check Upload Meta Data File
    And I choose the meta data file "hello.xml.json"
    And I choose the resource file "hello.xml.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "hello.xml"
    And I clicked on component "hello.xml"
    And I click "Template" tab
    And I enter garbage value in template at text "<"
    And I click save button of "content_Template"
    And I click on ok button
    And I wait for "Saved"
    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumJvm" and click button "Generate JVM resources files and deploy as a service"
    Then I verify resource deploy error for file "hello.xml" and jvm "seleniumJvm"


  Scenario: deploy an individual web-server resource with error-metadata
    Given I logged in
    And I am in the configuration tab
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "seleniumWebserver"
    And I select resource file "httpd.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I wait for "Saved"
    And I clicked on component "seleniumWebserver"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify resource deploy error for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploy an group web-server resource with error-template
    Given I logged in
    And I am in the configuration tab
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
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "Web Servers"
    And I select resource file "httpd.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I click on ok button
    And I wait for "Saved"
    And I clicked on component "Web Servers"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify resource deploy error for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploy an group web-server resource with error-meta data
    Given I logged in
    And I am in the configuration tab
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
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "Web Servers"
    And I clicked on component "httpd.conf"
    And I click "Meta Data" tab
    And I enter garbage value in metadata
    When I click save button of "content_Meta_Data"
    Then I verify metaData error
    And I click on ok button
    And I clicked on component "httpd.conf"
    And I click "Meta Data" tab
    And I erase garbage value
    And I click save button of "content_Meta_Data"
    And I click on ok button
    And I wait for "Saved"


  Scenario: deploy an individual web-server resource with error-meta data
    Given I logged in
    And I am in the configuration tab
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "seleniumWebserver"
    And I clicked on component "httpd.conf"
    And I click "Meta Data" tab
    And I enter garbage value in metadata
    When I click save button of "content_Meta_Data"
    Then I verify metaData error
    And I click on ok button
    And I clicked on component "httpd.conf"
    And I click "Meta Data" tab
    And I erase garbage value
    And I click save button of "content_Meta_Data"
    And I wait for "Saved"


  Scenario: deploy an group web-server resource with error-template-Operations
    Given I logged in
    And I am in the configuration tab
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
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "Web Servers"
    And I select resource file "httpd.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I click on ok button
    And I wait for "Saved"
    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumWebserver" and click button "Generate the httpd.conf and deploy as a service"
    Then I verify resource deploy error for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploy an individual web-server resource with error-template-Operations
    Given I logged in
    And I am in the configuration tab
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on component "seleniumWebserver"
    And I select resource file "httpd.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I wait for "Saved"
    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumWebserver" and click button "Generate the httpd.conf and deploy as a service"
    Then I verify resource deploy error for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploying an resource with a garbage value in multiple resources in Jvm
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
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk1.8.0_92          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
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

    And I clicked on component "hello.xml"
    And I click "Template" tab
    And I enter garbage value in template at text "<"
    And I click save button of "content_Template"
    And I wait for "Saved"

    And I clicked on component "setenv.bat"
    And I click "Template" tab
    And I enter garbage value in template at text "SET JAVA_HOME"
    And I click save button of "content_Template"
    And I wait for "Saved"

    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumJvm" and click button "Generate JVM resources files and deploy as a service"
    Then I verify multiple resource deploy error for file "hello.xml" and file "setenv.bat" and jvm "seleniumJvm"


  Scenario: deploy an individual  web-server resource with error-template
    Given I logged in
    And I am in the configuration tab
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"
    Then I select resource file "httpd.conf"

    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I wait for "Saved"
    And I clicked on component "seleniumWebserver"
    And I right click resource file "httpd.conf"
    And I click deploy option
    And I click on yes button
    And I verify resource deploy error for file "httpd.conf" and webserver "seleniumWebserver"


  Scenario: deploy an individual  web-server resource with error-template and multiple resources
    Given I logged in
    And I am in the configuration tab
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
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |

    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd.conf"

    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd2.conf"
    And I fill in the webserver "Deploy Path" field with "httpd.resource.deploy.path" for web server "seleniumWebserver"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then I check for resource "httpd2.conf"


    Then I select resource file "httpd.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# This is the main Apache HTTP server configuration file"
    And I click save button of "content_Template"
    And I wait for "Saved"
    And I see save button of "content_Template" again

    And I expanded component "Web Servers"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    Then I select resource file "httpd2.conf"
    And I click "Template" tab
    And I enter garbage value in template at text "# In particular, see"
    And I click save button of "content_Template"
    And I wait for "Saved"


    When I am in the Operations tab
    And I expanded operations Group "seleniumGroup"
    And I choose the row of the component with name "seleniumWebserver" and click button "Generate the httpd.conf and deploy as a service"
    And I verify many resource deploy error for file "httpd.conf" and file "httpd2.conf" and webserver "seleniumWebserver"