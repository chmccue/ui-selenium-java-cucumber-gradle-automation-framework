@navigation @navigation-external @regression
Feature: External Site Navigation

Background:
  Given I have an open browser on test site

Scenario Outline: External Site Navigation: <resolution nav> nav
  And turn on skipStep if "<resolution nav>" contains "public top"
  Given I resize to mobile resolution
  And turn off skipStep
  And turn on skipStep if "<resolution nav>" contains "public mobile"
  And I click "Features" link on "<resolution nav>" nav
  And I click "Learn" link on "<resolution nav>" nav
  And turn off skipStep
  And I click "Prices" link on "<resolution nav>" nav
  And I click "Support.company" link on "<resolution nav>" nav
  And I click "Institutions" link on "<resolution nav>" nav
  And I click "Login" link on "<resolution nav>" nav
  And I click "Signup" link on "<resolution nav>" nav
  @smoke
  Examples:
    | resolution nav |
    | public top     |

  Examples:
    | resolution nav |
    | public mobile  |
