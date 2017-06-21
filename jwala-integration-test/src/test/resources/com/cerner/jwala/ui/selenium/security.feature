Scenario: User logs in

    Given I am on the login page
    When I fill in the "User Name" field with
    And I fill in the "Password" field with
    And I click on the login button
    Then I should see the main page

Scenario: User logs out

    Given I am on the main page
    When I click the logout link
    Then I should see the login page