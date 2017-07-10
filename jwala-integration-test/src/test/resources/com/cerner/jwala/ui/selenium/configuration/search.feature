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
      |name      |app1    |
      |webappContext      |\name1           |
      |group|group1|
    And I created a web app with the following parameters:
      |name      |zzz    |
      |webappContext      |\name2           |
      |group|group2|
    And I fill in the search field with "app"
    Then I see "app1" in the web app table
    And I don't see "zzz" in the table

  Scenario: Search within media

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      |mediaName      |apache-httpd-2.4.20    |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir      |d:\ctp|
    And I created a media with the following parameters:
      |mediaName      |ZZZ    |
      |mediaType      |Apache HTTPD           |
      |archiveFilename|apache-httpd-2.4.20.zip|
      |remoteDir     |c:\ctp    |
    And I fill in the search field with "ap"
    Then I see "apache-httpd-2.4.20" in the media table
    And I don't see "ZZZ" in the table

  Scenario: Search within web servers

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
      | webserverName      | Rahul webserver         |
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
      | webserverName      | Sharvari webserver      |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | group2             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    And I am in the web server tab
    And I fill in the web server search field with "Rah"
    Then I see "Rahul webserver" in the webserver table
    And I don't see "Sharvari webserver" in the table








