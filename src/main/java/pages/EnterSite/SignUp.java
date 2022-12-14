package pages.EnterSite;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SignUp extends EnterSiteBase {

  public SignUp(WebDriver driver) { super(driver); }

  private final By signUpPage = By.cssSelector(".create-account-container");
  private final By signUpPageForm = By.cssSelector("[class*='SignupForm-module_form']");

  private final By signUpCountryFld = By.cssSelector("#country");
  public final By signUpProvinceFld = By.cssSelector("#province");

  private final By signUpAgreeCheckbox = By.cssSelector("#agreement");

  private final By submitActivationPage = By.cssSelector("[class*='activateAccountContent']");
  private final By cancelActivationPage = By.cssSelector("#cancelsignup");

  public boolean fillOutSignupFormData(String section, String input) {
    By formFld;
    if (textHas(section, "country")) {
      formFld = signUpCountryFld;
    } else if (textHas(section, "state")) {
      formFld = signUpProvinceFld;
    } else  {
      Log.warn("Field '" + section + "' not implemented on Sign Up page");
      return false;
    }
    if (elementHas(formFld, input)) return true;
    return waitForElement(signUpPage, 5) && selectDropDown(formFld, input);
  }

  public boolean clickSignUpCheckbox(String checkboxName) {
    By checkbox = strToBy("//*[contains(text(), '" + checkboxName
        + "')]//..//..//*[contains(@class, 'checkbox_')]");
    if (textHas(checkboxName, "accept")) {
      checkbox = signUpAgreeCheckbox;
    }
    return clickOn(checkbox);
  }

  public boolean onSignUpPageSection(String section) {
    By pageSection;
    if (textHas(section, "account activation")) {
      pageSection = submitActivationPage;
    } else if (textHas(section, "cancel")) {
      pageSection = cancelActivationPage;
    } else {
      // assumes signup/create account
      pageSection = signUpPageForm;
    }
    return waitForElement(pageSection, 5);
  }
}
