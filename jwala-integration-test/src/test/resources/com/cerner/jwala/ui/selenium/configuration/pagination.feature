Feature: Pagination

Scenario: Web Server Pagination

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "group1"
    And I created a group with the name "group2"
    And I am in the media tab
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir        |
    And I am in the web server tab
    And I select the dropdown of "webserver" with option "100"
    And I created a web server with the following parameters:
      | webserverName      | WebServer1          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer2          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer3          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer4          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer5          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer6          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer7          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer8          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer9          |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer10         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer11         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer12         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer13         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer14         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer15         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer16         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer17         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer18         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer20         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer21         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group2              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer22         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer23         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer24         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer25         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer26         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer27         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer28         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    And I created a web server with the following parameters:
      | webserverName      | WebServer29         |
      | hostName           | host1           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | group1              |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |
    When I click the column header with the label "Name"
    Then I see "WebServer29" in the webserver table


Scenario: Group Pagination

    Given I logged in
    And I am in the Configuration tab
    And I am in the group tab
    And I created a group with the name "Group1"
    And I created a group with the name "Group2"
    And I created a group with the name "Group3"
    And I created a group with the name "Group4"
    And I created a group with the name "Group5"
    And I created a group with the name "Group6"
    And I created a group with the name "Group7"
    And I created a group with the name "Group8"
    And I created a group with the name "Group9"
    And I created a group with the name "Group10"
    And I created a group with the name "Group11"
    And I created a group with the name "Group12"
    And I created a group with the name "Group13"
    And I created a group with the name "Group14"
    And I created a group with the name "Group15"
    And I created a group with the name "Group16"
    And I created a group with the name "Group17"
    And I created a group with the name "Group18"
    And I created a group with the name "Group19"
    And I created a group with the name "Group20"
    And I created a group with the name "Group21"
    And I created a group with the name "Group22"
    And I created a group with the name "Group23"
    And I created a group with the name "Group24"
    And I created a group with the name "Group25"
    And I created a group with the name "Group26"
    When I click the next page button
    Then I see the text "Page 2/2"
    When I click the previous page button
    Then I see the text "Page 1/2"