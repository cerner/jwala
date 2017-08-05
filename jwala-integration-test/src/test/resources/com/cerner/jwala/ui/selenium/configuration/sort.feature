Feature:Sort

  Scenario: Sort groups
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "MMMGroup"
    And I created a group with the name "ZZZGroup"
    And I created a group with the name "AAAGroup"
    When I click the column header with the label "Group Name"
    Then I see first item "AAAGroup"
    When I click the column header with the label "Group Name"
    Then I see first item "ZZZGroup"

  Scenario: Sort web-apps
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web apps tab
    And I created a web app with the following parameters:
      | webappName  | AAAApp |
      | contextPath | \name1 |
      | group       | group1 |
    And I created a web app with the following parameters:
      | webappName  | ZZZApp |
      | contextPath | \name2 |
      | group       | group2 |
    When I click the column header with the label "WebApp Name"
    Then I see first item "AAAApp"
    When I click the column header with the label "WebApp Name"
    Then I see first item "ZZZApp"

  Scenario: Sort media with name
    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaaMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    When I click the column header with the label "Name"
    Then I see first item "aaaMedia"
    When I click the column header with the label "Name"
    Then I see first item "zzzMedia"

  Scenario: Sort media with type

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaaMedia                |
      | mediaType       | Apache Tomcat           |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    When I click the column header with the label "Type"
    Then I see first item "zzzMedia"
    When I click the column header with the label "Type"
    Then I see first item "aaaMedia"


  Scenario: Sort media with remote directory
    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaaMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "zzzMedia"
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "aaaMedia"


  Scenario: Sort web servers with name
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | media.remote.dir        |
      | webserverName      | ZZZZZ                   |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group1                  |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | media.remote.dir        |
      | webserverName      | AAAAAA                  |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group2                  |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button with attribute "Name"
    Then I see first item "AAAAAA"
    When I click on the sort button with attribute "Name"
    Then I see first item "ZZZZZ"


  Scenario: Sort web servers with host
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZWebServer      |
      | hostName           | aHost               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAAWebServer     |
      | hostName           | zHost               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Host"
    Then I see first item "ZZZZZWebServer"
    When I click on the sort button with attribute "Host"
    Then I see first item "AAAAAAWebServer"


  Scenario: Sort web servers with port
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | zWebServer          |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | aWebServer          |
      | hostName           | localhost           |
      | portNumber         | 82                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Port"
    Then I see first item "zWebServer"
    When I click on the sort button with attribute "Port"
    Then I see first item "aWebServer"


  Scenario: Sort web servers with HTTPS port
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | zWebServer          |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | aWebServer          |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 444                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "HTTPS Port"
    Then I see first item "zWebServer"
    When I click on the sort button with attribute "HTTPS Port"
    Then I see first item "aWebServer"


  Scenario: Sort web servers with Group
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "aGroup"
    And I created a group with the name "zGroup"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZWebServer      |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | aGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAAWebServer     |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | zGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Group"
    Then I see first item "ZZZZZWebServer"
    When I click on the sort button with attribute "Group"
    Then I see first item "AAAAAAWebServer"


  Scenario: Sort web servers with Apache HTTPD
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a media with the following parameters:
      | mediaName       | aaaMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZWebServer |
      | hostName           | localhost      |
      | portNumber         | 80             |
      | httpsPort          | 443            |
      | group              | group1         |
      | apacheHttpdMediaId | aaaMedia       |
      | statusPath         | /apache_pb.png |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAAWebServer |
      | hostName           | localhost       |
      | portNumber         | 80              |
      | httpsPort          | 443             |
      | group              | group1          |
      | apacheHttpdMediaId | zzzMedia        |
      | statusPath         | /apache_pb.png  |
    When I click on the sort button with attribute "Apache HTTPD"
    Then I see first item "ZZZZZWebServer"
    When I click on the sort button with attribute "Apache HTTPD"
    Then I see first item "AAAAAAWebServer"


  Scenario: Sort  jvms with Name
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | aaaHost              |
      | portNumber | 1000                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | zzzHost              |
      | portNumber | 9999                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |

    When I click on the sort button with attribute "Name"
    Then I see first item "aaaJvm"
    When I click on the sort button with attribute "Name"
    Then I see first item "zzzJvm"

  Scenario: Sort  jvms with Group
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "aaagroup1"
    And I created a group with the name "yyygroup1"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | zzzHost              |
      | portNumber | 1000                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | aaagroup1            |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | aaaHost              |
      | portNumber | 9999                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | yyygroup1            |

    When I click on the sort button with attribute "Group"
    Then I see first item "zzzJvm"
    When I click on the sort button with attribute "Group"
    Then I see first item "aaaJvm"


  Scenario: Sort  jvms with hostName
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | aaaHost              |
      | portNumber | 9999                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |

    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | zzzHost              |
      | portNumber | 100                  |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    When I click on the sort button with attribute "Host"
    Then I see first item "zzzJvm"
    When I click on the sort button with attribute "Host"
    Then I see first item "aaaJvm"


  Scenario: Sort  jvms with Port
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | aaaHost              |
      | portNumber | 9999                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | zzzHost              |
      | portNumber | 1000                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    When I click on the sort button with attribute "HTTP"
    Then I see first item "zzzJvm"
    When I click on the sort button with attribute "HTTP"
    Then I see first item "aaaJvm"

  Scenario: Sort  jvms with JDK
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | zzzjdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | aaajdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | aaaHost              |
      | portNumber | 1000                 |
      | jdk        | zzzjdk1.8.0_92       |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | zzzHost              |
      | portNumber | 9999                 |
      | jdk        | aaajdk1.8.0_92       |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    When I click on the sort button with attribute "JDK"
    Then I see first item "zzzJvm"
    When I click on the sort button with attribute "JDK"
    Then I see first item "aaaJvm"


  Scenario: Sort  jvms with HTTPS
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm               |
      | hostName   | aaaHost              |
      | portNumber | 1000                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm               |
      | hostName   | zzzHost              |
      | portNumber | 9999                 |
      | jdk        | jdk1.8.0_92          |
      | tomcat     | apache-tomcat-7.0.55 |
      | group      | group1               |
    When I click on the sort button with attribute "HTTPS"
    Then I see first item "aaaJvm"
    When I click on the sort button with attribute "HTTPS"
    Then I see first item "zzzJvm"


  Scenario: Sort  jvms with TomcatMedia
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a media with the following parameters:
      | mediaName       | aaaapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm                  |
      | hostName   | localHost               |
      | portNumber | 100                     |
      | jdk        | jdk1.8.0_92             |
      | tomcat     | zzzapache-tomcat-7.0.55 |
      | group      | group1                  |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm                  |
      | hostName   | localHost               |
      | portNumber | 100                     |
      | jdk        | jdk1.8.0_92             |
      | tomcat     | aaaapache-tomcat-7.0.55 |
      | group      | group1                  |
    When I click on the sort button with attribute "Tomcat"
    Then I see first item "zzzJvm"
    When I click on the sort button with attribute "Tomcat"
    Then I see first item "aaaJvm"
