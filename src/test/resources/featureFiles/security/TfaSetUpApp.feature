@security @teardown-non-login-tfa-off @regression
Feature: Security: Shared tfa testing with the 2FA app

Background:
  Given I have an open browser on test site

Scenario Outline: Security: <tfa type> tfa: login tfa=<login tfa>: 2FA App: Validate Content, Turn Tfa on and off
  When I log into site with params:
    | country | us |
    | tfa     | <login tfa> |
  And I click "Security > tfa" link on "user menu" nav
  When I turn "on" tfa for "<tfa type>"
  And I verify text not found in "modal":
    | View setup key |
  And I submit correct authenticator code
  Then I confirm "success, enabled" alert
  And turn on skipStep if "<tfa type>" contains "(Login|Master)"
  And I count "<checked count>" checked checkboxes found in modal
  And I confirm and close tfa settings modal
  Then I confirm "success, updated" alert and close alert message
  And turn off skipStep
  Then I verify tfa settings:
    | <tfa type> | on | 2FA app |

  When I turn "off" tfa for "<tfa type>" and click modal button "confirm"
  Then I confirm "success, disabled" alert and close alert message
  And I verify tfa settings:
    | <tfa type> | off |

  Examples:
    | tfa type   | checked count | login tfa |
    | funding    | 1             | true      |
    | trading    | 3             | false     |
    | master key |               | false     |
