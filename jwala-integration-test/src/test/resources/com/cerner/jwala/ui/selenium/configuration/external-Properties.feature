Feature: External property

  Scenario: Add external property
    Given I logged in
    And I am in the configuration tab
    And I am in the resource tab
    When I click external property
    And I click ext property add resource
    And I choose the resource file "hctProperties.tpl"
    And I click on ok button
    Then I verify external property
    When I click ext property data tree
    Then I verify dataTree "active.directory.domain" and value "usmlvv1d0a"

  Scenario: Override external property
    Given I logged in
    And I am in the configuration tab
    And I am in the resource tab
    When I click external property
    And I click ext property add resource
    And I choose the resource file "hctProperties.tpl"
    And I click on ok button
    When I click external property
    And I click ext property add resource
    And I choose the resource file "hctRoleMappingProperties.tpl"
    Then I verify external property override message
    And I click on ok button
    And I verify external property
    And I click ext property data tree
    And I verify dataTree "security.group.names.tjmx" and value "grp-manager-jmx"

  Scenario: Modify external property
    Given I logged in
    And I am in the configuration tab
    And I am in the resource tab
    When I click external property
    And I click ext property add resource
    And I choose the resource file "hctProperties.tpl"
    And I click on ok button
    And I add property "team=ctp"
    And I click save button of "content_Template_Data"
    And I wait for "Saved"
    And I verify edit "team=ctp"


  Scenario: Delete external Property
    Given I logged in
    And I am in the configuration tab
    And I am in the resource tab
    When I click external property
    And I click ext property add resource
    And I choose the resource file "hctProperties.tpl"
    And I click on ok button
    And I click external properties check-box
    And I click the delete icon
    And I click on yes button
    And I verify ext properties is deleted