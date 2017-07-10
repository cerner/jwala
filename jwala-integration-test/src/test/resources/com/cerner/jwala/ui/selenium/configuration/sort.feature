Feature: Sort


  Scenario: Sort groups

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "MMM"
    And I created a group with the name "ZZZ"
    And I created a group with the name "AAA"
    When I click on the sort button of "Group "
    Then I see first item "AAA"
    When I click on the sort button of "Group "
    Then I see first item "ZZZ"

  Scenario: Sort web-apps

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web apps tab
    And I created a web app with the following parameters:
      |name      |AAAApp    |
      |webappContext      |\name1           |
      |group|group1|
    And I created a web app with the following parameters:
      |name      |ZZZApp    |
      |webappContext      |\name2           |
      |group|group2|
    When I click on the sort button of "WebApp "
    Then I see first item "AAAApp"
    When I click on the sort button of "WebApp "
    Then I see first item "ZZZApp"





  Scenario: Sort media with name

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      |mediaName      |aaa   |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    And I created a media with the following parameters:
      |mediaName      |zzz   |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    When I click on the sort button of media with "Name"
    Then I see first item "aaa"
    When I click on the sort button of media with "Name"
    Then I see first item "zzz"

  Scenario: Sort media with type

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      |mediaName      |aaa   |
      |mediaType      |Apache Tomcat           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    And I created a media with the following parameters:
      |mediaName      |zzz   |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    When I click on the sort button of media with "Type"
    Then I see first item "zzz"
    When I click on the sort button of media with "Type"
    Then I see first item "aaa"

  Scenario: Sort media with remote directory

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      |mediaName      |aaa   |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    And I created a media with the following parameters:
      |mediaName      |zzz   |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |c:\ctp|
    When I click on the sort button of media with "Remote Target Directory"
    Then I see first item "zzz"
    When I click on the sort button of media with "Remote Target Directory"
    Then I see first item "aaa"





  Scenario: Sort web servers with name

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | ZZZZZ         |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group1             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | c:/ctp                  |
      | webserverName      | AAAAAA       |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group2             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button of web server with attribute "Name"
    Then I see first item "AAAAAA"
    When I click on the sort button of web server with attribute "Name"
    Then I see first item "ZZZZZ"


  Scenario: Sort web servers with host

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | ZZZZZ         |
      | hostName           | aHost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group1             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | c:/ctp                  |
      | webserverName      | AAAAAA       |
      | hostName           | zHost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group2             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button of web server with attribute "Host"
    Then I see first item "ZZZZZ"
    When I click on the sort button of web server with attribute "Host"
    Then I see first item "AAAAAA"


  Scenario: Sort web servers with port

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | zServer         |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group1             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | c:/ctp                  |
      | webserverName      | aServer       |
      | hostName           | localhost               |
      | portNumber         | 82                     |
      | httpsPort          | 443                     |
      | group              | group2             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button of web server with attribute "Port"
    Then I see first item "zServer"
    When I click on the sort button of web server with attribute "Port"
    Then I see first item "aServer"



  Scenario: Sort web servers with HTTPS port

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | zServer         |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group1             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | c:/ctp                  |
      | webserverName      | aServer       |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 444                     |
      | group              | group2             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button of web server with attribute "HTTPS Port"
    Then I see first item "zServer"
    When I click on the sort button of web server with attribute "HTTPS Port"
    Then I see first item "aServer"


  Scenario: Sort web servers with Group

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "aGroup"
    And I created a group with the name "zGroup"
    And I am in the web server tab
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | ZZZZZ         |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | aGroup             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I created a web server with the following parameters:
      | mediaName          | apache-httpd-2.4.21     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | c:/ctp                  |
      | webserverName      | AAAAAA       |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | zGroup             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    When I click on the sort button of web server with attribute "Group"
    Then I see first item "ZZZZZ"
    When I click on the sort button of web server with attribute "Group"
    Then I see first item "AAAAAA"




