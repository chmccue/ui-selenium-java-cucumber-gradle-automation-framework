@login @regression
Feature: Login Page

Background:
  Given I have an open browser on test site

Scenario Outline: Login: Successful <tier> Login
  When I click "sign-in" link on "footer" nav
  And I log into site with params:
    | tier | <tier> |
    | tfa  | false  |
  And I click "Logout" link on "<logout menu>" nav
  Then I am on the logged out landing page

  Examples:
    | tier | logout menu |
    | t0   | user menu   |
    | t2   | footer      |

Scenario: Login: Validate errors
  Then I enter login fields and confirm invalid error alert:
    | Abc                               | 123       | |
    | overMaximumLengthOf32Charactersss | Abcd12345 | |
  When I enter the following on Login page:
    | User     | inval\dch@r@cter$ |
    | Password | Abc#1234 |
  Then I confirm "error, Enter a valid username" tooltip

Scenario Outline: Successful Login with 2FA <tfa> enabled
  And I log into site with params:
    | tfa_method | <tfa> |
  Then I click "Logout" link on "footer" nav
  And I am on the logged out landing page

  Examples:
    | tfa |
    | password |
  @smoke
  Examples:
    | tfa |
    | app |
