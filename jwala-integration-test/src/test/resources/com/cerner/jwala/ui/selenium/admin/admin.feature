Feature: Admin Utilities

Scenario: Test encryption, properties reload and manifest.mf

    Given I logged in
    And I see client details "jwala.client.details" "jwala.data.mode"
    And I am in the admin tab

    # test encryption
    When I fill in the "data to be secured" field with "password"
    And I click the admin's tab ">>> Encrypt >>>" button
    Then I see the "Encryption Succeeded" message

    # test properties management
    And I see the "Properties Management" heading
    And I see "jgroups.cluster.name" in the "Properties Management" text box

    # test manifest.mf content
    And I see the "MANIFEST.MF" heading
    And I see "Implementation-Title" in the "MANIFEST.MF" text box
    And I see "Implementation-Version" in the "MANIFEST.MF" text box