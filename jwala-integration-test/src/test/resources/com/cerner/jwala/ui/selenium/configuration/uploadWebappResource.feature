Feature: Upload Resource - Webbapp Node

  Scenario:Webapp Node
    Given I logged in
    And I am in the configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a webapp with following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello       |
      | group       | seleniumGroup  |
    And I am in the resource tab
    And I expanded component "seleniumGroup"
    And I expanded component "Web Apps"
    And I clicked on component "seleniumWebapp"
    And I clicked on add resource
    And I fill in the "Deploy Name" field with "hello-world.war"
    And I fill in the "Deploy Path" field with "C:\ctp\app\instances"
    And I choose the resource file "hello-world.war"
    When I click the upload resource dialog ok button
    Then check resource uploaded successful