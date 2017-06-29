Feature: Add, Edit and Delete a Group

Scenario: Add a group

    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    When I click the add group button
    And I see the group add dialog
    And I fill in the "Group Name" field with "GROUP_X"
    And I click the group add dialog ok button
    Then I see "GROUP_X" in the group table