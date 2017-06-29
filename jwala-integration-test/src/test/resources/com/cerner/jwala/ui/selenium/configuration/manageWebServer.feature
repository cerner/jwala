Feature: Add, Edit and Delete a Web Server

Scenario: Add Web Server

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "GROUP_FOR_ADD_WEBSERVER_TEST"
    And I am in the web server tab
    When I click the add web server button
    And I see the web server add dialog
    And I fill in the "Web Server Name" field with "WEBSERVER_X"
    And I fill in the "Host Name" field with "localhost"
    And I fill in the "HTTP Port" field with "80"
    And I fill in the "HTTPS Port" field with "443"
    And I select the "Status Path" field
    And I select the "Apache HTTPD" field "APACHE_HTTPD"
    And I select the group "GROUP_FOR_ADD_WEBSERVER_TEST"
    And I click the add web server dialog ok button
    Then I see "WEBSERVER_X" in the webserver table