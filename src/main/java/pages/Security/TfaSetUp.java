package pages.Security;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.GlobalModal;

import static utils.tfaApp.getTfaAppCode;

public class TfaSetUp extends GlobalModal {

  public TfaSetUp(WebDriver driver) { super(driver); }

  final By modalSetUpTfa = strToBy("[data-testid*='setup dialog']");
  public final By modalSettingsTfa = strToBy("[data-testid*='settings dialog']");
  final By modalDisableTfa = strToBy("[data-testid*='disabling dialog']");
  private final By setUpTab = strToBy("[class*='segmentedOption' i]");
  private final By setUpSelectedTab = strToBy("[class*='segmentedOption' i][class*='selected' i]");

  // 2FA App
  private final By modalAuthSecretKey = strToBy("[name='tfaSecret' i]");
  private final By modalAuthEnterCodeFld = strToBy("[name='otpCode' i]");
  public static String secretKeyText;

  public boolean getAndEnterAuthKey() {
    if (!elementHas(strToBy("body"), "Hide setup key")) {
      clickOnText(".pointer", "View setup key");
    }
    secretKeyText = regexParse(getElementText(modalAuthSecretKey), "^.*?$");
    Log.addStepLog("authenticator key generated: " + secretKeyText);
    int runAttempts = 3;
    while (runAttempts > 0) {
      runAttempts -= 1;
      if (enterSecurityModalField(getTfaAppCode(secretKeyText))
          && globalClickModalBtn("confirm") && waitForNotVisible(modalSetUpTfa, 10)) {
        return true;
      }
      hardWait(1);
    }
    return false;
  }

  public boolean enterSecurityModalField(String toEnter) {
    return clearAndEnterText(modalAuthEnterCodeFld, toEnter);
  }

  public boolean clickTfaTab(String tabValue) {
    return  waitForElement(setUpTab, 2) && clickOnText(byToStr(setUpTab), tabValue)
        && waitForElement(setUpSelectedTab, 1);
  }

  public boolean confirmAndCloseSettingsModal() {
    int runAttempts = 2;
    boolean closeModal = false;
    while (runAttempts > 0 && !closeModal) {
      closeModal = globalClickModalBtn("confirm") && waitForNotVisible(modalSettingsTfa, 5);
      runAttempts -= 1;
    }
    return closeModal;
  }
}
