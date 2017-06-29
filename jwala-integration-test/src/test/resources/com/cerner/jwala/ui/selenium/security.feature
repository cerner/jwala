Feature: Security

Scenario: User logs in using different user accounts

    Given I load predefined user accounts
    Then I use those accounts to login successfully and unsuccessfully

Scenario: User logs in

    Given I am on the login page
    When I fill in the "User Name" field with a valid user name
    And I fill in the "Password" field with a valid password
    And I click the login button
    Then I should see the main page


Scenario: User logs out

    Given I logged in
    When I click the logout link
    Then I am redirected to the login page