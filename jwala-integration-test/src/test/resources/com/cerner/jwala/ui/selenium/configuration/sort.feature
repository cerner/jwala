<<<<<<< HEAD
Feature:Sort

  Scenario: Sort groups
=======
Feature: Sort Table
    Groups can be sorted in ascending or descending order based on the column header that a user clicked
    JVMs can be sorted in ascending or descending order based on the column header that a user clicked
    Web Servers can be sorted in ascending or descending order based on the column header that a user clicked
    Web Applications can be sorted in ascending or descending order based on the column header that a user clicked
    Media can be sorted in ascending or descending order based on the column header that a user clicked


Scenario: Sort Group by Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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

<<<<<<< HEAD
  Scenario: Sort web-apps
=======

Scenario: Sort Web Applications by Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web apps tab
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

<<<<<<< HEAD
  Scenario: Sort media with name
=======

Scenario: Sort Media by Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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

<<<<<<< HEAD
  Scenario: Sort media with type
=======

Scenario: Sort Media by Type
>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680

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


<<<<<<< HEAD
  Scenario: Sort media with remote directory
=======
Scenario: Sort Media by Remote Directory

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | aaaMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | zDir                    |
    And I created a media with the following parameters:
      | mediaName       | zzzMedia                |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | aDir                    |
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "zzzMedia"
    When I click the column header with the label "Remote Target Directory"
    Then I see first item "aaaMedia"


<<<<<<< HEAD
  Scenario: Sort web servers with name
=======
Scenario: Sort Web Servers by Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
    When I click the column header with the label "Name"
    Then I see first item "AAAAAA"
    When I click the column header with the label "Name"
    Then I see first item "ZZZZZ"


<<<<<<< HEAD
  Scenario: Sort web servers with host
=======
Scenario: Sort Web Servers by Host Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
          | webserverName      | AAAAAAWebServer     |
          | hostName           | zHost               |
          | portNumber         | 80                  |
          | httpsPort          | 443                 |
          | group              | group2              |
          | apacheHttpdMediaId | apache-httpd-2.4.20 |
          | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | ZZZZZWebServer      |
      | hostName           | aHost               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    When I click the column header with the label "Host"
    Then I see first item "ZZZZZWebServer"
    When I click the column header with the label "Host"
    Then I see first item "AAAAAAWebServer"


<<<<<<< HEAD
  Scenario: Sort web servers with port
=======
Scenario: Sort Web Servers by HTTP Port

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
    When I click the column header with the label "Port"
    Then I see first item "zWebServer"
    When I click the column header with the label "Port"
    Then I see first item "aWebServer"


<<<<<<< HEAD
  Scenario: Sort web servers with HTTPS port
=======
Scenario: Sort Seb Servers by HTTPS Port

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
    When I click the column header with the label "HTTPS Port"
    Then I see first item "zWebServer"
    When I click the column header with the label "HTTPS Port"
    Then I see first item "aWebServer"


<<<<<<< HEAD
  Scenario: Sort web servers with Group
=======
Scenario: Sort Web Servers by Group Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
    When I click the column header with the label "Group"
    Then I see first item "ZZZZZWebServer"
    When I click the column header with the label "Group"
    Then I see first item "AAAAAAWebServer"


<<<<<<< HEAD
  Scenario: Sort web servers with Apache HTTPD
=======
Scenario: Sort Web Servers by Apache HTTPD Media

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
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
    When I click the column header with the label "Apache HTTPD"
    Then I see first item "ZZZZZWebServer"
    When I click the column header with the label "Apache HTTPD"
    Then I see first item "AAAAAAWebServer"


<<<<<<< HEAD
  Scenario: Sort  jvms with Name
=======
Scenario: Sort JVMs by Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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

    When I click the column header with the label "Name"
    Then I see first item "aaaJvm"
    When I click the column header with the label "Name"
    Then I see first item "zzzJvm"

<<<<<<< HEAD
  Scenario: Sort  jvms with Group
=======

Scenario: Sort JVMs by Group Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "aaagroup1"
    And I created a group with the name "yyygroup1"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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

    When I click the column header with the label "Group"
    Then I see first item "zzzJvm"
    When I click the column header with the label "Group"
    Then I see first item "aaaJvm"


<<<<<<< HEAD
  Scenario: Sort  jvms with hostName
=======
Scenario: Sort JVMs by Host Name

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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
    When I click the column header with the label "Host"
    Then I see first item "zzzJvm"
    When I click the column header with the label "Host"
    Then I see first item "aaaJvm"


<<<<<<< HEAD
  Scenario: Sort  jvms with Port
=======
Scenario: Sort JVMs by HTTP Port

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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
    When I click the column header with the label "HTTP"
    Then I see first item "zzzJvm"
    When I click the column header with the label "HTTP"
    Then I see first item "aaaJvm"

<<<<<<< HEAD
  Scenario: Sort  jvms with JDK
=======

Scenario: Sort JVMs by JDK

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | zzzjdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | aaajdk1.8.0_92          |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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
    When I click the column header with the label "JDK"
    Then I see first item "zzzJvm"
    When I click the column header with the label "JDK"
    Then I see first item "aaaJvm"


<<<<<<< HEAD
  Scenario: Sort  jvms with HTTPS
=======
Scenario: Sort jvms by HTTPS Port

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55                 |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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
    When I click the column header with the label "HTTPS"
    Then I see first item "aaaJvm"
    When I click the column header with the label "HTTPS"
    Then I see first item "zzzJvm"


<<<<<<< HEAD
  Scenario: Sort  jvms with TomcatMedia
=======
Scenario: Sort JVMs by Tomcat Media

>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk1.8.0_92             |
      | mediaType       | JDK                     |
      | archiveFilename | jdk1.8.0_92.zip |
      | remoteDir       | media.remote.dir        |
    And I created a media with the following parameters:
      | mediaName       | zzzapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
    And I created a media with the following parameters:
      | mediaName       | aaaapache-tomcat-7.0.55              |
      | mediaType       | Apache Tomcat                        |
      | remoteDir       | media.remote.dir                     |
      | archiveFilename | apache-tomcat-7.0.55.zip |
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
    When I click the column header with the label "Tomcat"
    Then I see first item "zzzJvm"
    When I click the column header with the label "Tomcat"
<<<<<<< HEAD
    Then I see first item "aaaJvm"
=======
    Then I see first item "aaaJvm"
>>>>>>> 1684a283504a31261d90d01c619f994cf6eb2680
