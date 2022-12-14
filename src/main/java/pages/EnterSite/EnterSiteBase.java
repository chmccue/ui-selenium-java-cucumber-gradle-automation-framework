package pages.EnterSite;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.clickUtils;

public class EnterSiteBase extends clickUtils {

  EnterSiteBase(WebDriver driver) { super(driver); }

  private final By activationKeyField = By.cssSelector("#activationKey");
  private final By emailField = By.cssSelector("[name='email']");
  final By userField = By.cssSelector("[name='username']");
  final By passwordField = By.cssSelector("[name='password']");
  private final By masterKeyField = By.cssSelector("[name='secret']");
  public final By submitBtn = By.cssSelector("[type='submit']");

  private String randomUser = "";

  public boolean fillOutSharedFormField(String section, String input) {
    return fillOutSharedFormField(section, input, true);
  }

  private boolean fillOutSharedFormField(String section, String input, boolean retry) {
    By formFld;
    boolean randomInput = input.equalsIgnoreCase("random");
    if (randomInput) {
      input = randomStrGenerator(getRandomNum(8, 20, 20));
      if (textHas(section, "(email|user)") && randomUser.isEmpty()) {
        randomUser = "automation" + input;
      }
    }

    if (textHas(section, "activation key")) {
      formFld = activationKeyField;
    } else if (textHas(section, "email")) {
      formFld = emailField;
      if (randomInput) input = "fe-qa+" + randomUser + "@lich.io";
    } else if (textHas(section, "user")) {
      formFld = userField;
      if (randomInput) input = randomUser;
    } else if (textHas(section, "password")) {
      formFld = passwordField;
    } else if (textHas(section, "master key")) {
      formFld = masterKeyField;
    } else {
      Log.warn("Field '" + section + "' not implemented on Activate Account page");
      return false;
    }

    if (randomInput) {
      Log.addStepLog("Actual random text to enter in " + section + " field: " + input);
    }
    boolean enterAndValidate = waitForElement(formFld, 3) && clearAndEnterText(formFld, input)
        && regexMatch(getMyAttribute(formFld, "value"), input);
    if (!enterAndValidate && retry) {
      return fillOutSharedFormField(section, input, false);
    }
    return enterAndValidate;
  }

  public boolean clickSubmitBtn() {
    return clickOn(submitBtn);
  }
}
