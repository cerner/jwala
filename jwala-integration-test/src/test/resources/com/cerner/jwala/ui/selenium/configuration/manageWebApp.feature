Feature: Add, Edit and Delete a Web Application

Scenario: Add Web Application

    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "GROUP_FOR_ADD_WEBAPP_TEST"
    And I am in the Web Apps tab
    When I click the add web app button
    And I see the web app add dialog
    And I fill in the web app "Name" field with "WEBAPP_X"
    And I fill in the web app "Context Path" field with "webapp"
    And I associate the web app to the following groups:
        |GROUP_FOR_ADD_WEBAPP_TEST|
    And I click the Unpack WAR checkbox
    And I click the add web app dialog ok button
    Then I see the following web app details in the web app table:
        |name   |WEBAPP_X                 |
        |context|webapp                   |
        |group  |GROUP_FOR_ADD_WEBAPP_TEST|
    
    
    # Edit Web Application

    When I click on "WEBAPP_X" link to open Edit Web App dialog
    Then I see Unpack WAR checkbox is "checked"
    And I see Secure checkbox is "unchecked"
    
    When I click the Unpack WAR checkbox
    And I click the Secure checkbox
    And I click the add web app dialog ok button
    Then I see the following web app details in the web app table:
        |name   |WEBAPP_X                 |
        |context|webapp                   |
        |group  |GROUP_FOR_ADD_WEBAPP_TEST|
    
    When I click on "WEBAPP_X" link to open Edit Web App dialog
    Then I see Unpack WAR checkbox is "unchecked"
    And I see Secure checkbox is "checked"


    # Delete Web Application

    When I click the add web app dialog ok button
    And I see the following web app details in the web app table:
        |name   |WEBAPP_X                 |
        |context|webapp                   |
        |group  |GROUP_FOR_ADD_WEBAPP_TEST|
    And I select the "WEBAPP_X" application row in the table
    And I click the delete web app button
    And I see the web app delete dialog
    And I click the delete web app dialog Yes button
    Then I see the "The table is empty!" message