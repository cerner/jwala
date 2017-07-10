Feature: Pagination
  Scenario: Pagination within groups


    Given I logged in
    And I am in the configuration tab
    And I am in the group tab
    And I created a group with the name "A1"
    And I created a group with the name "A2"
    And I created a group with the name "A3"
    And I created a group with the name "A4"
    And I created a group with the name "A5"
    And I created a group with the name "A6"
    And I created a group with the name "A7"
    And I created a group with the name "A8"
    And I created a group with the name "A9"
    And I created a group with the name "A10"
    And I created a group with the name "A11"
    And I created a group with the name "A12"
    And I created a group with the name "A13"
    And I created a group with the name "A14"
    And I created a group with the name "A15"
    And I created a group with the name "A16"
    And I created a group with the name "A17"
    And I created a group with the name "A18"
    And I created a group with the name "A19"
    And I created a group with the name "A20"
    And I created a group with the name "A21"
    And I created a group with the name "A22"
    And I created a group with the name "A23"
    And I created a group with the name "A24"
    And I created a group with the name "A25"
    And I created a group with the name "A26"
    And I click the right button
    Then I see the text "Page 2/2"
    And I click the left button
    Then I see the text "Page 1/2"
