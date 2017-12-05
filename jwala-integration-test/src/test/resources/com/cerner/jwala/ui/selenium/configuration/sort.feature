Feature: Sort Table
  Groups can be sorted in ascending or descending order based on the column header that a user clicked
  JVMs can be sorted in ascending or descending order based on the column header that a user clicked
  Web Servers can be sorted in ascending or descending order based on the column header that a user clicked
  Web Applications can be sorted in ascending or descending order based on the column header that a user clicked
  Media can be sorted in ascending or descending order based on the column header that a user clicked


  Scenario: Sort Group by Name

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "MMMGroup"
    And I created a group with the name "ZZZGroup"
    And I created a group with the name "AAAGroup"
    When I click the column header with the label "Group Name"
    Then I see first item "AAAGroup"
    When I click the column header with the label "Group Name"
    Then I see first item "ZZZGroup"


  Scenario: Sort Web Applications by Name

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the Web Apps tab
    And I created a web app with the following parameters:
      | webappName  | AAAApp       |
      | contextPath | aContextPath |
      | group       | group1       |
    And I created a web app with the following parameters:
      | webappName  | ZZZApp       |
      | contextPath | aContextPath |
      | group       | group2       |
    When I click the column header with the label "WebApp Name"
    Then I see first item "AAAApp"
    When I click the column header with the label "WebApp Name"
    Then I see first item "ZZZApp"


  Scenario: Sort Media

    Given I logged in
    And I am in the Configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | zMedia                     |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | zDir                       |
    And I created a media with the following parameters:
      | mediaName       | aMedia                      |
      | mediaType       | Apache Tomcat               |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | bDir                        |
    And I created a media with the following parameters:
      | mediaName       | mMedia            |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | aDir              |
    When I click the column header with the label "Name"
    Then I see first item "aMedia"
    When I click the column header with the label "Name"
    Then I see first item "zMedia"
    When I click the column header with the label "Type"
    Then I see first item "zMedia"
    When I click the column header with the label "Type"
    Then I see first item "mMedia"
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "mMedia"
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "zMedia"


  Scenario: Sort Web Servers

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a group with the name "group3"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aMedia                     |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    And I created a media with the following parameters:
      | mediaName       | mMedia                     |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    And I created a media with the following parameters:
      | mediaName       | zMedia                     |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | aWebserver     |
      | hostName           | host1          |
      | portNumber         | 9000           |
      | httpsPort          | 20005          |
      | group              | group1         |
      | apacheHttpdMediaId | zMedia         |
      | statusPath         | ws.status.path |
    And I created a web server with the following parameters:
      | webserverName      | mWebserver     |
      | hostName           | host1          |
      | portNumber         | 8999           |
      | httpsPort          | 20008          |
      | group              | group3         |
      | apacheHttpdMediaId | aMedia         |
      | statusPath         | ws.status.path |
    And I created a web server with the following parameters:
      | webserverName      | zWebserver     |
      | hostName           | host1          |
      | portNumber         | 7000           |
      | httpsPort          | 30000          |
      | group              | group2         |
      | apacheHttpdMediaId | mMedia         |
      | statusPath         | ws.status.path |
    When I click the column header with the label "Name"
    Then I see first item "aWebserver"
    When I click the column header with the label "Name"
    Then I see first item "zWebserver"
    When I click the column header with the label "Port"
    Then I see first item "zWebserver"
    When I click the column header with the label "Port"
    Then I see first item "aWebserver"
    When I click the column header with the label "HTTPS Port"
    Then I see first item "aWebserver"
    When I click the column header with the label "HTTPS Port"
    Then I see first item "zWebserver"
    When I click the column header with the label "Group"
    Then I see first item "aWebserver"
    When I click the column header with the label "Group"
    Then I see first item "mWebserver"
    When I click the column header with the label "Apache HTTPD"
    Then I see first item "mWebserver"
    When I click the column header with the label "Apache HTTPD"
    Then I see first item "aWebserver"

  Scenario: Sort JVMs

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "aGroup"
    And I created a group with the name "mGroup"
    And I created a group with the name "zGroup"
    And I created a media with the following parameters:
      | mediaName       | ajdk              |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | mjdk              |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | zjdk              |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | aTomcat                     |
      | mediaType       | Apache Tomcat               |
      | remoteDir       | media.remote.dir            |
      | archiveFilename | apache.tomcat.media.archive |
    And I created a media with the following parameters:
      | mediaName       | mTomcat                     |
      | mediaType       | Apache Tomcat               |
      | remoteDir       | media.remote.dir            |
      | archiveFilename | apache.tomcat.media.archive |
    And I created a media with the following parameters:
      | mediaName       | zTomcat                     |
      | mediaType       | Apache Tomcat               |
      | remoteDir       | media.remote.dir            |
      | archiveFilename | apache.tomcat.media.archive |
    And I created a jvm with the following parameters:
      | jvmName    | aJvm    |
      | hostName   | host1   |
      | portNumber | 4001    |
      | jdk        | mjdk    |
      | tomcat     | zTomcat |
      | group      | mGroup  |
    And I created a jvm with the following parameters:
      | jvmName    | mJvm    |
      | hostName   | host1   |
      | portNumber | 9999    |
      | jdk        | ajdk    |
      | tomcat     | mTomcat |
      | group      | aGroup  |
    And I created a jvm with the following parameters:
      | jvmName    | zJvm    |
      | hostName   | host1   |
      | portNumber | 8000    |
      | jdk        | zjdk    |
      | tomcat     | aTomcat |
      | group      | zGroup  |
    When I click the column header with the label "Name"
    Then I see first item "aJvm"
    When I click the column header with the label "Name"
    Then I see first item "zJvm"
    When I click the column header with the label "Group"
    Then I see first item "mJvm"
    When I click the column header with the label "Group"
    Then I see first item "zJvm"
    When I click the column header with the label "HTTP"
    Then I see first item "aJvm"
    When I click the column header with the label "HTTP"
    Then I see first item "mJvm"
    When I click the column header with the label "HTTPS"
    Then I see first item "aJvm"
    When I click the column header with the label "HTTPS"
    Then I see first item "mJvm"
    When I click the column header with the label "JDK"
    Then I see first item "mJvm"
    When I click the column header with the label "JDK"
    Then I see first item "zJvm"
    When I click the column header with the label "Tomcat"
    Then I see first item "zJvm"
    When I click the column header with the label "Tomcat"
    Then I see first item "aJvm"