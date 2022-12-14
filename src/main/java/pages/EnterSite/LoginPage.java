package pages.EnterSite;

import static utils.tfaApp.getTfaAppCode;

import main.Core;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.GlobalNav.TopNavBar;

public class LoginPage extends EnterSiteBase {

  public LoginPage(WebDriver driver) { super(driver); }

  private String currentLoginTfaAppCode;
  private final By loginPage = strToBy("[data-testid='login-basic' i]:not(.hidden)");
  public final By loginTfaField = strToBy("input[name='otp']");
  public final By loginBtnTfa = strToBy("[data-testid='login-tfa-inputs'] .submit");

  private final By loginHelpMenu = strToBy("[data-testid='need-help']");
  private final By loginHelpUserSection = strToBy("[data-testid='recover-username-form']");
  private final By loginHelpPwSection = strToBy("[data-testid='recover-password-form']");
  private final By loginOtpSection = strToBy("#otpbypass");
  private final By loginSubHelpCloseBtn = strToBy("[data-testid='close-self-service-button']");
  private final By loginHelpCloseBtn = strToBy("[data-testid='protect-account-close-button");

  public boolean enterLoginUserNameAndPw(String user, String pw) {
    return goToLoginPage()
        && clearAndEnterText(userField, user) && clearAndEnterText(passwordField, pw);
  }

  private boolean goToLoginPage() {
    if (exists(loginPage)) return true; // checks if already on login page
    return new TopNavBar(driver).publicTopNavLoginPresent(true)
        && clickOn(setHrefVar("sign-in")) && waitForElement(loginPage, 5)
        && waitForElement(userField, 3);
  }

  public boolean enterLoginTfa(String tfa, boolean goodAppTfa) {
    // if user tfa_method is "app", we get the dynamic code using the user's stored secret key.
    if (goodAppTfa && Core.user != null && textHas(Core.user.tfa_method, "app")) {
      if (currentLoginTfaAppCode != null) {
        for (int i = 0; i <= 10; i++) {
          if (!textHas(currentLoginTfaAppCode, getTfaAppCode(tfa))) break;
          hardWait(3);
        }
      }
      currentLoginTfaAppCode = getTfaAppCode(tfa);
      tfa = getTfaAppCode(tfa);
    }
    return waitForElement(loginTfaField, 5) && clearAndEnterText(loginTfaField, tfa);
  }

  public boolean enterLoginAndPressSubmit(String username, String password) {
    return enterLoginAndPressSubmit(username, password, "non-tfa account");
  }

  public boolean enterLoginAndPressSubmit(String username, String password, String tfa) {
    boolean loginLandingPage = enterLoginUserNameAndPw(username, password) && clickOn(submitBtn);
    boolean loginTfaPage =
        textHas(tfa, "non-tfa account") || enterLoginTfa(tfa, true) && clickOn(loginBtnTfa);
    return loginLandingPage && loginTfaPage;
  }

  public boolean loginExpectedSuccess(String username, String password, int retry) {
    return loginExpectedSuccess(username, password, "non-tfa account", retry);
  }

  // If site gets stuck on login page, will refresh and check if user is taken from login page.
  // If yes, test returns true. if not, retry logic continues. Enter retry=0 to disable retrying.
  public boolean loginExpectedSuccess(String username, String password, String tfa, int retry) {
    boolean loginSuccess = enterLoginAndPressSubmit(username, password, tfa)
        && waitForNotVisible(loginPage, 5);
    if (!loginSuccess && retry > 0) {
      pageRefresh();
      if (textHas(currentUrl(), "/sign-in")) {
        return loginExpectedSuccess(username, password, tfa, retry - 1);
      } else {
        return true;
      }
    }
    return loginSuccess;
  }

  public boolean onLoginPageSection(String section) {
    By pageSection = loginPage;
    if (textHas(section, "password")) pageSection = loginHelpPwSection;
    else if (textHas(section, "username")) pageSection = loginHelpUserSection;
    else if (textHas(section, "bypass")) pageSection = loginOtpSection;
    else if (textHas(section, "^help")) pageSection = loginHelpMenu;
    return waitForElement(pageSection, 5);
  }

  public boolean clickCloseLoginHelpSection(String section) {
    By closeMenu = textHas(section, "^help") ? loginHelpCloseBtn : loginSubHelpCloseBtn;
    return clickOn(closeMenu) && waitForNotVisible(closeMenu, 1);
  }
}
