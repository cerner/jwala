Feature: Upload Resource - Webserver Node

  Scenario:Webserver Node
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "Rahul group"
    And I created a webserver with following parameters:
      | mediaName          | apache-httpd-2.4.20     |
      | mediaType          | Apache HTTPD            |
      | archiveFilename    | apache-httpd-2.4.20.zip |
      | remoteDir          | d:/ctp                  |
      | webserverName      | Rahul webserver         |
      | hostName           | localhost               |
      | portNumber         | 80                      |
      | httpsPort          | 443                     |
      | group              | Rahul group             |
      | apacheHttpdMediaId | apache-httpd-2.4.20     |
      | statusPath         | /apache_pb.png          |
    Then I see "Rahul webserver" in the webserver table
