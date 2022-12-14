@trade @trade-navigation @regression
Feature: Trade Navigation

Background:
  Given I have an open browser on test site
@smoke
Scenario Outline: Trade: Navigate Trade Subsections
  When I log into site with params:
    | funds | false |
  And I select and verify "<language>" option from footer language menu
  Then I click "Trade" link on "main account" nav
  And I click "Orders" link on "sub account" nav
  And I verify Orders table is on the Orders Trade page
  And I click "Positions" link on "sub account" nav
  And I click "Trades" link on "sub account" nav
  And I click "New-Order" link on "sub account" nav
  And I click "Intermediate" link on "order form" nav
  And I click "Advanced" link on "order form" nav
  And I click "Simple" link on "order form" nav

  Examples:
    | language |
    | en-us    |
    | es-es    |
    | fr-fr    |
