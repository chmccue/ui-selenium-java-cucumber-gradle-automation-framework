@login @login-forgot @regression
Feature: Login Page Forgot Interaction

Background:
  Given I have an open browser on test site
  When I click "sign-in" link on "footer" nav
  And I click on text "button::Trouble signing in?"

Scenario: Login: Help Menu: Reset Password: Submit button disabled with negative field entry
  When I click on text "button::resetting my password"
  Then I am on "reset password" Login page
  When I enter the following on Reset Password page:
  | User  | test |
  | Email | hello |
  Then I confirm submit button disabled on page
  When I enter the following on Reset Password page:
  | Email | hello@example. |
  Then I confirm submit button disabled on page

Scenario: Login: Help Menu: Reset Password: Submit form and click Close Button
  When I click on text "button::resetting my password"
  Then I am on "reset password" Login page
  When I enter the following on Reset Password page:
  | Email | random |
  | User  | random |
  And I click submit button on page
  Then I verify text found:
  | We've received your password recovery request |
  When I click close "reset password" Login page
  Then I am on "help" Login page
