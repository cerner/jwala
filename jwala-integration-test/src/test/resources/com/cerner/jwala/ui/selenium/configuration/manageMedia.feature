Feature: Add, Edit and Delete a Media

Scenario: Add a media

    Given I logged in
    And I am in the configuration tab
    And I am in the media tab
    When I click the add media button
    And I see the media add dialog
    And I fill in the "Media Name" field with "apache-httpd-2.4.20"
    And I select "Media Type" item "Apache HTTPD"
    And I choose the media archive file "apache-httpd-2.4.20.zip"
    And I fill in the "Remote Directory" field with "d:/ctp"
    And I click the add media dialog ok button
    Then I see "apache-httpd-2.4.20" in the media table