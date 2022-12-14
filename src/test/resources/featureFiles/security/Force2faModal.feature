@security @force-2fa
Feature: Force 2FA Modal Tests

Background:
  Given I have an open browser on test site
  When I log into site to check force 2fa modal
  Then I verify text found in "modal title":
    | Secure Your Account |

Scenario: Force 2FA Modal: Press Skip button to close
  When I click on text "button::Skip for now"
  Then I verify modal is "not found"

Scenario: Force 2FA Modal: Press Close button to close
  When I click modal button "close"
  Then I verify modal is "not found"
