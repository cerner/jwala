Feature:Sort

  Scenario: Sort groups
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "MMM"
    And I created a group with the name "ZZZ"
    And I created a group with the name "AAA"
    When I click on the sort button of component "Group " with attribute "Name"
    Then I see first item "AAA"
    When I click on the sort button of component "Group " with attribute "Name"
    Then I see first item "ZZZ"

  Scenario: Sort web-apps
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web apps tab
    And I created a web app with the following parameters:
      | name          | AAAApp |
      | webappContext | \name1 |
      | group         | group1 |
    And I created a web app with the following parameters:
      | name          | ZZZApp |
      | webappContext | \name2 |
      | group         | group2 |
    When I click on the sort button of component "WebApp " with attribute "Name"
    Then I see first item "AAAApp"
    When I click on the sort button of component "WebApp " with attribute "Name"
    Then I see first item "ZZZApp"

  Scenario: Sort media with name

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaa                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | zzz                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    When I click on the sort button of component "" with attribute "Name"
    Then I see first item "aaa"
    When I click on the sort button of component "" with attribute "Name"
    Then I see first item "zzz"

  Scenario: Sort media with type

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaa                     |
      | mediaType       | Apache Tomcat           |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | zzz                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    When I click on the sort button of component "" with attribute "Type"
    Then I see first item "zzz"
    When I click on the sort button of component "" with attribute "Type"
    Then I see first item "aaa"


  Scenario: Sort media with remote directory

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaa                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | zzz                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | c:\ctp                  |
    When I click on the sort button of component "" with attribute "Remote Target Directory"
    Then I see first item "zzz"
    When I click on the sort button of component "" with attribute "Remote Target Directory"
    Then I see first item "aaa"


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
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
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
      | remoteDir          | c:/ctp                  |
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
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZ               |
      | hostName           | aHost               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAA              |
      | hostName           | zHost               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Host"
    Then I see first item "ZZZZZ"
    When I click on the sort button with attribute "Host"
    Then I see first item "AAAAAA"


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
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | zServer             |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | aServer             |
      | hostName           | localhost           |
      | portNumber         | 82                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Port"
    Then I see first item "zServer"
    When I click on the sort button with attribute "Port"
    Then I see first item "aServer"


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
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | zServer             |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | aServer             |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 444                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "HTTPS Port"
    Then I see first item "zServer"
    When I click on the sort button with attribute "HTTPS Port"
    Then I see first item "aServer"


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
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZ               |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | aGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAA              |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | zGroup              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click on the sort button with attribute "Group"
    Then I see first item "ZZZZZ"
    When I click on the sort button with attribute "Group"
    Then I see first item "AAAAAA"


  Scenario: Sort web servers with Apache HTTPD
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a media with the following parameters:
      | mediaName       | aaaapache-httpd-2.4.20  |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | zzzapache-httpd-2.4.20  |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | c:\ctp                  |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZ                  |
      | hostName           | localhost              |
      | portNumber         | 80                     |
      | httpsPort          | 443                    |
      | group              | group1                 |
      | apacheHttpdMediaId | aaaapache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png         |
    And I created a web server with the following parameters:
      | webserverName      | AAAAAA                 |
      | hostName           | localhost              |
      | portNumber         | 80                     |
      | httpsPort          | 443                    |
      | group              | group1                 |
      | apacheHttpdMediaId | zzzapache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png         |
    When I click on the sort button with attribute "Apache HTTPD"
    Then I see first item "ZZZZZ"
    When I click on the sort button with attribute "Apache HTTPD"
    Then I see first item "AAAAAA"


  Scenario: Sort  jvms with Name
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | aaaHost              |
      | http   | 1000                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | zzzHost              |
      | http   | 9999                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |

    When I click on the sort button with attribute "Name"
    Then I see first item "aaa"
    When I click on the sort button with attribute "Name"
    Then I see first item "zzz"

  Scenario: Sort  jvms with Group
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "aaagroup1"
    And I created a group with the name "yyygroup1"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | zzzHost              |
      | http   | 1000                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | aaagroup1            |
    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | aaaHost              |
      | http   | 9999                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | yyygroup1            |

    When I click on the sort button with attribute "Group"
    Then I see first item "zzz"
    When I click on the sort button with attribute "Group"
    Then I see first item "aaa"


  Scenario: Sort  jvms with hostName
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | aaaHost              |
      | http   | 9999                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |

    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | zzzHost              |
      | http   | 100                  |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    When I click on the sort button with attribute "Host"
    Then I see first item "zzz"
    When I click on the sort button with attribute "Host"
    Then I see first item "aaa"


  Scenario: Sort  jvms with Port
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | aaaHost              |
      | http   | 9999                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | zzzHost              |
      | http   | 1000                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    When I click on the sort button with attribute "HTTP"
    Then I see first item "zzz"
    When I click on the sort button with attribute "HTTP"
    Then I see first item "aaa"

  Scenario: Sort  jvms with JDK
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | zzzjdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | aaajdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | aaaHost              |
      | http   | 1000                 |
      | jdk    | zzzjdk1.8.0_92       |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |

    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | zzzHost              |
      | http   | 9999                 |
      | jdk    | aaajdk1.8.0_92       |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    When I click on the sort button with attribute "JDK"
    Then I see first item "zzz"
    When I click on the sort button with attribute "JDK"
    Then I see first item "aaa"


  Scenario: Sort  jvms with HTTPS
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | aaa                  |
      | host   | aaaHost              |
      | http   | 1000                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    And I created a jvm with the following parameters:
      | name   | zzz                  |
      | host   | zzzHost              |
      | http   | 9999                 |
      | jdk    | jdk1.8.0_92          |
      | tomcat | apache-tomcat-7.0.55 |
      | group  | group1               |
    When I click on the sort button with attribute "HTTPS"
    Then I see first item "aaa"
    When I click on the sort button with attribute "HTTPS"
    Then I see first item "zzz"


  Scenario: Sort  jvms with TomcatMedia
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the jvm tab
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92-windows.zip |
      | remoteDir       | c:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | zzzapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a media with the following parameters:
      | mediaName       | aaaapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | c:\stp                               |
      | archiveFilename | apache-tomcat-7.0.55-windows-x64.zip |
    And I created a jvm with the following parameters:
      | name   | aaa                     |
      | host   | localHost               |
      | http   | 100                     |
      | jdk    | jdk1.8.0_92             |
      | tomcat | zzzapache-tomcat-7.0.55 |
      | group  | group1                  |
    And I created a jvm with the following parameters:
      | name   | zzz                     |
      | host   | localHost               |
      | http   | 100                     |
      | jdk    | jdk1.8.0_92             |
      | tomcat | aaaapache-tomcat-7.0.55 |
      | group  | group1                  |
    When I click on the sort button with attribute "Tomcat"
    Then I see first item "zzz"
    When I click on the sort button with attribute "Tomcat"
    Then I see first item "aaa"
