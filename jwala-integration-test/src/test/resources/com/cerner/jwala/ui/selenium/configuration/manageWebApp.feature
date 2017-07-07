Feature: Add, Edit and Delete a Web Server

Scenario: Add Web Application

    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "GROUP_FOR_ADD_WEBAPP_TEST"
    And I am in the web apps tab
    When I click the add web app button
    And I see the web app add dialog
    And I fill in the web app "Name" field with "WEBAPP_X"
    And I fill in the web app "Context Path" field with "webapp"
    And I associate the web app to the following groups:
        |GROUP_FOR_ADD_WEBAPP_TEST|
    And I click the add web app dialog ok button
    Then I see "WEBAPP_X" in the web app table