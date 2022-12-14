package steps;

import static main.Core.siteUrl;
import static main.Core.user;
import static pages.Security.PasswordSetUp.enteredStaticKey;
import static pages.Security.TfaSetUp.secretKeyText;
import static utils.matcherUtils.textHas;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import main.Base;
import main.Core;
import main.Log;
import main.UserProvisioner;
import org.junit.Assert;
import pages.EnterSite.LoginPage;
import pages.Overview.OverviewPage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginPageSteps implements Base {

  private final LoginPage login = new LoginPage(driver);
  private final OverviewPage overview = new OverviewPage(driver);
  public static boolean loggedIn = false;

  @After(value = "@teardown-login-if-needed", order = 4)
  public void teardownLogBackIntoSite() {
    if (!LoginPageSteps.loggedIn && user != null
        && (secretKeyText != null || enteredStaticKey != null)) {
      Map<String, String> login = new HashMap<>();
      login.put("locked user", "");
      new LoginPageSteps().loginToSiteWithParams(login);
    }
  }

  @When("^I log into site with params:$")
  public void loginToSiteWithParams(Map<String, String> params) {
    if (Core.skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I log into site with params: " + params.toString());
    loginXTimes(1, params);
    Assert.assertTrue(overview.overviewLoadingBarDone(3));
    page.closeModalIfOpen(); // handles force 2fa or other modal popup on login.
    Assert.assertTrue(overview.overviewLandingPageLoaded());
    Assert.assertTrue(navMenu.dismissLaunchPadToolTip());
  }

  @When("^I log into site to check force 2fa modal$")
  public void loginToSiteWithParamsFor2fa() {
    Log.info("I log into site to check force 2fa modal");
    Map<String, String> params = new HashMap<>();
    params.put("country", "us");
    params.put("tfa", "false");
    user = UserProvisioner.getUser(params);
    Log.addStepLog("Selected user: " + user.username);
    Assert.assertTrue(login.loginExpectedSuccess(user.username, user.password, 3));
    Assert.assertTrue(page.waitForElement(page.globalModalContent, 15));
    loggedIn = true;
  }

  @And("^I unlock latest user provisioner account$")
  public void unlockLatestUserProvisionerAccount() {
    UserProvisioner.releaseUser(user);
  }

  @When("^I login with new password \"([^\"]*)\" and tfa \"([^\"]*)\"$")
  public void loginWith(String password, String tfa) {
    Log.info("I login with new password " + password + " and tfa " + tfa);
    Assert.assertTrue(login.enterLoginAndPressSubmit(user.username, password, tfa));
    loggedIn = true;
  }

  @And("^I enter login fields and confirm (invalid error alert|button enabled|button disabled):$")
  public void attemptLoginAndConfirmExpectedResult(String expected, DataTable data) {
    Log.info("I enter login fields and confirm " + expected + ":\n" + data);
    List<List<String>> rows = data.asLists(String.class);
    for (List<String> row : rows) {
      String user = row.get(0) != null ? row.get(0) : "";
      String pw = row.get(1) != null ? row.get(1) : "";
      String tfa = row.size() > 2 && row.get(2) != null ? row.get(2) : "";
      boolean enterFields;
      boolean refreshPageAtEnd = false;
      if (textHas(expected, "invalid error alert")) {
        enterFields = tfa.isEmpty()
            ? login.enterLoginAndPressSubmit(user, pw)
            : login.enterLoginAndPressSubmit(user, pw, tfa);
        page.hardWait(0.1);
      } else if (tfa.isEmpty()) {
        // if not checking invalid error and tfa is empty, assume we are testing main login page
        enterFields = login.enterLoginUserNameAndPw(user, pw);
      } else { // assumes tfa enabled validation
        enterFields = login.enterLoginUserNameAndPw(user, pw)
            && login.clickOn(login.submitBtn) && login.enterLoginTfa(tfa, true)
            && page.enabled(login.loginBtnTfa);
        refreshPageAtEnd = true;
      }
      boolean statusCheck;
      if (textHas(expected, "enabled")) {
        statusCheck = page.enabled(login.submitBtn);
      } else if (textHas(expected, "disabled")) {
        statusCheck  = !page.enabled(login.submitBtn);
      } else { // assumes invalid alert error validation
        statusCheck = page.checkAlertTypeAndMessage(page.strToLst("fail, invalid"))
            && page.closeAlertMessage();
      }
      if (!enterFields || !statusCheck) {
        steps.fails.add("Fail checking: user=" + user + ", pw=" + pw + ", tfa=" + tfa);
      }
      if (refreshPageAtEnd) page.pageRefresh();
    }
    steps.failHandler();
  }

  @And("^I enter the following on (Login|Recover Username|Reset Password|Bypass 2FA) page:$")
  public void enterInLoginFields(String section, Map<String, String> params) {
    Log.info("I enter the following on " + section + " page:\n" + params.toString());
    for (Map.Entry<String, String> item: params.entrySet()) {
      if (textHas(item.getKey(), "(email|key|user|pass)")) {
        String value = item.getValue() == null ? "" : item.getValue();
        if (textHas(item.getKey(), "user") && textHas(value, "^locked user")) {
          value = user.username;
          Log.addStepLog("username being used: " + value);
        } else if (textHas(item.getKey(), "email") && textHas(value, "^locked email")) {
          value = user.email;
          Log.addStepLog("email being used: " + value);
        }
        Assert.assertTrue(login.fillOutSharedFormField(item.getKey(), value));
      }
    }
  }

  @And("^I am on \"([^\"]*)\" Login page$")
  public void onLoginSection(String section) {
    Log.info("I am on " + section + " Login page");
    Assert.assertTrue(login.onLoginPageSection(section));
  }

  @And("^I click close \"([^\"]*)\" Login page")
  public void closeLoginSection(String section) {
    Log.info("I click close " + section + " Login page");
    Assert.assertTrue(login.clickCloseLoginHelpSection(section));
  }

  @When("^I begin log in and stop at the tfa page with params:$")
  public void loginToSiteUpToTfaPageWithParams(Map<String, String> params) {
    Log.info("I begin log in and stop at the tfa page with params: " + params.toString());
    if (user == null) user = UserProvisioner.getUser(params);
    Log.addStepLog("Selected user: " + user.username);
    if (user.tfa_code == null) Assert.fail("parameters must have tfa to be used by this method.");
    Assert.assertTrue(login.enterLoginUserNameAndPw(user.username, user.password)
        && page.clickOn(login.submitBtn));
    Assert.assertTrue(page.waitForElement(login.loginTfaField, 5));
  }

  // Enter "locked user" in params key if you need to re-login with a user within the same test.
  // Note the test will fail if you haven't already logged in and locked a user when doing this.
  @And("^I log into site \"(\\d{1,2})\" times? with params:$")
  public void loginXTimes(int runCount, Map<String, String> params) {
    Log.info("I log into site " + runCount + " times with params:\n " + params);
    if (!params.containsKey("locked user")) user = UserProvisioner.getUser(params);
    Log.addStepLog("Selected user: " + user.username);
    runCount += 1;
    for (int i = 1; i < runCount; i++) {
      if (i == 1 || i == runCount - 1 || i % 5 == 0) Log.addStepLog("Starting login number " + i);
      Assert.assertTrue(user.tfa_code == null
          ? login.loginExpectedSuccess(user.username, user.password, 3)
          : login.loginExpectedSuccess(user.username, user.password, user.tfa_code, 3));
      Assert.assertTrue(overview.overviewLoadingBarDone(3));
      page.closeModalIfOpen(); // handles force 2fa or other modal popup on login.
      Assert.assertTrue(page.waitForElement(overview.overviewPageContent, 5));
      Assert.assertTrue(navMenu.dismissLaunchPadToolTip());
      loggedIn = true;
      if (i < (runCount - 1)) {
        new NavigationSteps().clickItemInNav("logout", "user menu");
        loggedIn = false;
        coreEnv.clearAllCookies();
        driver.get(siteUrl);
      }
    }
  }

  @And("^I fail login on \"(password|tfa)\" with params:$")
  public void loginFail(String section, Map<String, String> params) {
    Log.info("I fail login on " + section + " with params:\n " + params);
    if (user == null) user = UserProvisioner.getUser(params);
    Assert.assertTrue(textHas(section, "password")
        ? login.enterLoginAndPressSubmit(user.username, "bad p@ssw0rd")
        : login.enterLoginAndPressSubmit(user.username, user.password)
        && login.enterLoginTfa("000000", false) && page.clickOn(login.loginBtnTfa));
  }
}