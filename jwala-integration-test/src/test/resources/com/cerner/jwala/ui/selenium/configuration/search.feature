Feature: Search Items
  Group, JVM, web server, web application and media tables should be searchable


  Scenario: Search for a Group in the Group Table

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "MMMGroup"
    And I created a group with the name "ZZZGroup"
    When I fill in the search field with "MM"
    Then I see "MMMGroup" in the group table
    And I don't see "ZZZGroup" in the table


  Scenario: Search for a Web Application in the Web Application Table

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the Web Apps tab
    And I created a web app with the following parameters:
      | webappName  | application  |
      | contextPath | /contextPath |
      | group       | group1       |
    And I created a web app with the following parameters:
      | webappName  | zzzApp |
      | contextPath | \name2 |
      | group       | group2 |
    When I fill in the search field with "appli"
    Then I see "application" web app table
    And I don't see "zzzApp" in the table


  Scenario: Search for a Media in the Media Table

    Given I logged in
    And I am in the Configuration tab
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | yyy                        |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    And I created a media with the following parameters:
      | mediaName       | ZZZApacheMedia             |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    When I fill in the search field with "yy"
    Then I see "yyy" in the media table
    And I don't see "ZZZApacheMedia" in the table


  Scenario: Search for a Web Server in the Web Server Table

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media         |
      | mediaType       | Apache HTTPD               |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir           |
    And I am in the web server tab
    And I created a web server with the following parameters:
      | webserverName      | aWebserver         |
      | hostName           | host1              |
      | portNumber         | 80                 |
      | httpsPort          | 443                |
      | group              | group1             |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png     |
    And I created a web server with the following parameters:
      | webserverName      | myWebserver        |
      | hostName           | host1              |
      | portNumber         | 80                 |
      | httpsPort          | 443                |
      | group              | group2             |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png     |
    And I am in the web server tab
    When I fill in the search field with "my"
    Then I see "myWebserver" in the webserver table
    And I don't see "aWebserver" in the table


  Scenario: Search for JVM in the JVM Table

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I created a media with the following parameters:
      | mediaName       | jdk.media         |
      | mediaType       | JDK               |
      | archiveFilename | jdk.media.archive |
      | remoteDir       | media.remote.dir  |
    And I created a media with the following parameters:
      | mediaName       | apache.tomcat.media         |
      | mediaType       | Apache Tomcat               |
      | remoteDir       | media.remote.dir            |
      | archiveFilename | apache.tomcat.media.archive |
    And I created a jvm with the following parameters:
      | jvmName    | aaaJvm              |
      | hostName   | host1               |
      | portNumber | 122                 |
      | jdk        | jdk.media           |
      | tomcat     | apache.tomcat.media |
      | group      | group1              |
    And I created a jvm with the following parameters:
      | jvmName    | zzzJvm              |
      | hostName   | host1               |
      | portNumber | 404                 |
      | jdk        | jdk.media           |
      | tomcat     | apache.tomcat.media |
      | group      | group1              |
    And I am in the jvm tab
    When I fill in the search field with "aa"
    Then I see "aaaJvm" in the jvm table
    And I don't see "zzzJvm" in the table