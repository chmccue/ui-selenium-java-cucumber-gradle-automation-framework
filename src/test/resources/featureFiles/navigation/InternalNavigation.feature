@navigation @regression
Feature: Internal/Logged In Navigation

Background:
  Given I have an open browser on test site
@smoke
Scenario Outline: Internal Site Navigation: Language=<language>: User Menu Nav
  When I log into site with params:
    | country | us    |
    | tier    | t4    |
    | funds   | false |
  And I select and verify "<language>" option from footer language menu
  Then I make sure sub sections expand and collapse in user menu
  Then I click "Settings > Account" link on "user menu" nav
  Then I click "Settings > Notifications" link on "user menu" nav
  Then I click "Security > tfa" link on "user menu" nav
  Then I click "Security > api" link on "user menu" nav
  Then I click "Get Verified" link on "user menu" nav
  Then I click "Logout" link on "user menu" nav
  And I am on the logged out landing page

  Examples:
    | language |
    | en-us    |
    | it-it    |
    | es-es    |
