Feature: External property

  Scenario: Add external property
    Given I logged in
    And I am in the Configuration tab
    And I am in the resource tab
    When I click "Ext Properties" component
    And I click the add resource button
    And I choose the resource file "hctProperties.tpl"
    And I click the ok button
    Then I check for resource "ext.properties"
    When I expand "ext" node in data tree
    Then I verify dataTree has the key-value pair as "request.queue.jms.listener.receive.timeout" and "60000" respectively

  Scenario: Override external property
    Given I logged in
    And I am in the Configuration tab
    And I am in the resource tab
    When I click "Ext Properties" component
    And I click the add resource button
    And I choose the resource file "hctProperties.tpl"
    And I click the ok button
    When I click "Ext Properties" component
    And I click the add resource button
    And I choose the resource file "hctRoleMappingProperties.tpl"
    Then I verify external property override message
    And I click the ok button
    And I check for resource "ext.properties"
    When I click "Ext Properties" component
    And I expand "ext" node in data tree
    Then I verify dataTree has the key-value pair as "security.user.roles.0" and "endUser" respectively

  Scenario: Modify external property
    Given I logged in
    And I am in the Configuration tab
    And I am in the resource tab
    When I click "Ext Properties" component
    And I click the add resource button
    And I choose the resource file "hctProperties.tpl"
    And I click the ok button
    And I select the resource file "ext.properties"
    And I add property "team=ctp"
    And I click save button of edit box of "Template"
    And I wait for notification "Saved"
    And I verify edit "team=ctp"

  Scenario: Delete external Property
    Given I logged in
    And I am in the Configuration tab
    And I am in the resource tab
    When I click "Ext Properties" component
    And I click the add resource button
    And I choose the resource file "hctProperties.tpl"
    And I click the ok button
    And I click check-box for resourceFile "ext.properties"
    And I click the resource delete icon
    And I click yes button to delete a resource
    And I don't see "ext.properties"