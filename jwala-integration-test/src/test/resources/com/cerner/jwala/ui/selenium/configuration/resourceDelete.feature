# new feature
# Tags: optional

Feature: Deleting a resource

  Scenario: Delete an individual  web-server resource
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
    When I select resource file "httpd.conf"
    And I click resource file delete button
    And I click on ok button
    Then I don't see "httpd.conf"

  Scenario: Delete an group  web-server resource
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
    When I select resource file "httpd.conf"
    And I click resource file delete button
    And I click on ok button
    Then I don't see "httpd.conf"
    When I clicked on component "seleniumWebserver"
    And I check for resource "httpd.conf"


  Scenario: Delete a resource -individual jvm
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
    When I click resource file delete button
    And I click on ok button
    Then I don't see "hello.xml"

  Scenario: Delete a resource- group jvm
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
    When I click resource file delete button
    And I click on ok button
    Then I don't see "hello.xml"
    When I clicked on component "seleniumJvm"
    Then I check for resource "hello.xml"


  Scenario: Resource webapp delete
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
    When I click resource file delete button
    And I click on ok button
    Then I don't see "hct.properties"