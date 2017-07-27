Feature: Upload Resource - Webserver Node

  Scenario:Webserver Node
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache-httpd-2.4.20     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache-httpd-2.4.20.zip |
      | remoteDir       | d:/ctp                  |
    And I created a webserver with following parameters:
      | webserverName      | seleniumWebserver     |
      | hostName           | localhost           |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup         |
      | apacheHttpdMediaId | apache-httpd-2.4.20 |
      | statusPath         | /apache_pb.png      |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Servers"
    And I clicked on component "seleniumWebserver"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "httpd.conf"
    And I fill in the "Deploy Path" field with "C:\ctp\app\data\httpd"
    And I choose the resource file "httpdconf.tpl"
    When I click the upload resource dialog ok button
    Then check resource uploaded successful

