Feature: Search

  Scenario: Search within groups


    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "MMM"
    And I created a group with the name "ZZZ"
    And I fill in the search field with "MM"
    Then I see "MMM" in the group table
    And I don't see "ZZZ" in the table


  Scenario: Search within web app

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web apps tab
    And I created a web app with the following parameters:
      | name          | app1   |
      | webappContext | \name1 |
      | group         | group1 |
    And I created a web app with the following parameters:
      | name          | zzz    |
      | webappContext | \name2 |
      | group         | group2 |
    And I fill in the search field with "app"
    Then I see "app1" in the web app table
    And I don't see "zzz" in the table

  Scenario: Search within media

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:\ctp                  |
    And I created a media with the following parameters:
      | mediaName       | ZZZ                     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | c:\ctp                  |
    And I fill in the search field with "ap"
    Then I see "apache-httpd-2.4.20" in the media table
    And I don't see "ZZZ" in the table

  Scenario: Search within web servers

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
      | webserverName      | aWebserver          |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | myWebserver         |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I am in the web server tab
    And I fill in the search field with "my"
    Then I see "myWebserver" in the webserver table
    And I don't see "aWebserver" in the table


  Scenario: Search within  jvms

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
      | jvmName       | aaa                  |
      | hostName      | localHost            |
      | httpPort      | 122                  |
      | jdkMediaId    | jdk1.8.0_92          |
      | tomcatMediaId | apache-tomcat-7.0.55 |
      | group         | group1               |

    And I created a jvm with the following parameters:
      | jvmName       | zzz                  |
      | hostName      | localhost            |
      | httpPort      | 404                  |
      | jdkMediaId    | jdk1.8.0_92          |
      | tomcatMediaId | apache-tomcat-7.0.55 |
      | group         | group1               |

    And I am in the jvm tab
    And I fill in the search field with "aa"
    Then I see "aaa" in the JVM table
    And I don't see "zzz" in the table









