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
    And I am in the resource tab
    And I expanded group
    And I expanded webservers
    And I clicked on webserver
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the "Deploy Path" field with "C:\ctp\app\data\httpd"
    And I choose the resource file "httpdconf.tpl"
    And I click the upload resource dialog ok button
    Then check resource uploaded successful

