@security @security-api @teardown-delete-api-keys @regression
Feature: Security API page tests

Background:
  Given I have an open browser on test site
@smoke
Scenario: Security: API: Edit API key and update key
  When I log into site with params:
    | country | us |
    | funds   | false |
  And I click "Security > API" link on "user menu" nav
  When I go and create new API Key with name "auto"
  When I edit the "auto" API Key
  And I check the "Withdraw Funds" API key permissions

  When I enter "auto-updated" in "Key Name" API field
  And I enter "5" in "Nonce Window" API field
  And I toggle the "key expiration" switch on in API Settings
  And I set the calendar date for "key expiration" switch in API Settings
  And I toggle the "query start" switch on in API Settings
  And I set the calendar date for "query start" switch in API Settings
  And I toggle the "query end" switch on in API Settings
  And I set the calendar date for "query end" switch in API Settings
  And I click on text "button::save"
  Then I verify text found:
    | API key management |
  And I verify text not found:
    | ^auto$ |
