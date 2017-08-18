Feature: admin

  Scenario: Encrypting a password
    Given I logged in
    And I am in the admin tab
    When I fill in the data to be encrypted "password"
    And I click "Encrypt" button
    Then I verify text "Encryption Succeeded"

  Scenario: Properties
    Given I logged in
    And I am in the admin tab
    When I click "Reload" button
    And I verify header with text "Properties Management"
    And I see the text "commands.scripts-path" in properties management box

  Scenario: Manifest.mf
    Given I logged in
    And I am in the admin tab
    Then I verify header with text "MANIFEST.MF"
    And I see the text "Implementation-Title=jwala-webapp" in manifest box
    And I see the implementation version "implementation-version" in manifest box
    
