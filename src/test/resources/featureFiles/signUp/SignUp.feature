@sign-up @regression
Feature: Sign Up Section

Background:
  Given I have an open browser on test site
  When I click "Signup" link on "public top" nav
  And I am on "Create Account" Signup page
@smoke
Scenario: Create Account: Verify creating account for existing email goes to Account Activation page
  When I enter the following on Signup page:
    | Email    | fe-qa+automationIFgs1icR@lich.io |
    | Username | random |
    | Password | Az18%hf59012 |
    | Country  | United States |
    | State    | random |
  Then I confirm submit button disabled on page
  And I click "accept" checkbox on Signup page
  And I click submit button on page
  Then I am on "Account Activation" Signup page

Scenario: Create Account: Password Requirement Error Messages
  When I enter the following on Signup page:
    | Password | 123456789123 |
  And I click away from modal field
  Then I confirm "Contains at least one letter" error message found
  When I enter the following on Signup page:
    | Password | ahdhshahjsaa |
  And I click away from modal field
  Then I confirm "Contains at least one number" error message found
  When I enter the following on Signup page:
    | Password | ahdhshahjs12 |
  And I click away from modal field
  Then I confirm "Contains at least one special character" error message found
