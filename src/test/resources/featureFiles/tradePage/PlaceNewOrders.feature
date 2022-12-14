@trade @trade-new-orders @teardown-cancel-all-open-orders @regression
Feature: Trade New Order - Place New Orders

Background:
  Given I have an open browser on test site

Scenario Outline: Trade: New Order: <orderForm> Form: Place Limit Buy/Sell and Cancel
  When I log into site with params:
    | country     | us |
    | funds-trade | true |
  And I select "XBT/EUR" from asset ticker menu
  And I click "Trade" link on "main account" nav
  And I click "New-Order" link on "sub account" nav
  And I click "<orderForm>" link on "order form" nav
  And I click "Buy" item on "<orderForm>" order
  And I click "Limit" item on "<orderForm>" order
  And for limit enter ".1" amount and ".1" price on "<orderForm>" order
  Then I complete "<orderForm>" Buy order expecting "success"
  And I cancel last open order on New Order page
  @smoke
  Examples:
    | orderForm |
    | Simple    |
    | Advanced  |
