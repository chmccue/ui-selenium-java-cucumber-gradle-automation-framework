package steps;

import static main.Core.skipStep;
import static utils.matcherUtils.textHas;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.After;
import io.cucumber.java.en.When;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.Security.PasswordSetUp;
import steps.globals.GlobalModalSteps;
import java.util.List;

public class SecurityPageSteps implements Base {

  private static final PasswordSetUp security = new PasswordSetUp(driver);
  private final GlobalModalSteps modal = new GlobalModalSteps();
  private final String tfaOptions = "(login|funding|trading|master key|api[^\" ]*)";

  @After(value = "@teardown-non-login-tfa-off", order = 2)
  public void teardownTurnOffNonLoginTfa() {
    page.pageRefresh();
    page.closeModalWithEscapeKey(true);
    page.closeModalIfOpen();
    if (!textHas(page.currentUrl(), "/security/tfa")) {
      new NavigationSteps().clickItemInNav("Security > tfa", "user menu");
    }
    page.waitForElement(security.securityToggle, 10, true, 2);
    int runAttempts = 4;
    while (page.exists(security.securityToggleOn)) {
      security.clickToggleAndTurnOffTfa("any", true);
      page.pageRefresh();
      page.waitForElement(security.securityToggle, 5, true, 2);
      if ((runAttempts -= 1) == 0) break;
    }
    while (!security.checkTfaToggleStatus("api", "off")) {
      turnOnOffTfaAndClick("off", "api", "confirm");
    }
  }

  @When("^I turn \"(on|off)\" tfa for \"" + tfaOptions + "\"$")
  public void turnOnOffTfaToggleOnly(String onOrOff, String section) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I turn " + onOrOff + " tfa for " + section);
    Assert.assertFalse(security.checkTfaToggleStatus(section, onOrOff));
    Assert.assertTrue(security.clickToggleOpensTfaModal(
        section, onOrOff.equalsIgnoreCase("on")));
  }

  @When("^I turn \"(on|off)\" tfa for \""
      + tfaOptions + "\" and click modal button \"(confirm|cancel|close)\"$")
  public void turnOnOffTfaAndClick(String onOrOff, String section, String modalBtn) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    boolean onAndConfirmBtn = textHas(onOrOff, "on") && textHas(modalBtn, "confirm");
    if (textHas(onOrOff, "off") && textHas(modalBtn, "confirm")) {
      Assert.assertTrue(security.clickToggleAndTurnOffTfa(section, true));
    } else {
      turnOnOffTfaToggleOnly(onOrOff, section);
      Assert.assertTrue(page.globalClickModalBtn(modalBtn));
      modal.modalPresent(onAndConfirmBtn ? "found" : "not found");
    }
    if (onAndConfirmBtn) return;

    // inverts onOrOff if checking for cancel/close modal button press.
    onOrOff = textHas(onOrOff, "on") || textHas(modalBtn, "confirm") ? "off" : "on";
    Assert.assertTrue(security.checkTfaToggleStatus(section, onOrOff));
  }

  // Checks table for | tfa section | on/off status | specific tfa type set |
  // if row size is 2, only checks section and on/off. If row is 3 and specific type is null/empty,
  // skips specific tfa text assertion.
  @And("^I verify tfa settings:$")
  public void verifyAuthOnOff(DataTable dt) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I verify tfa settings:\n" + dt);
    List<List<String>> rows = dt.asLists(String.class);
    for (List<String> row : rows) {
      boolean checkTfaType = row.size() == 2 || row.get(2) == null
          || security.checkTfaMethodText(row.get(0), row.get(2));
      if (!security.checkTfaToggleStatus(row.get(0), row.get(1))
          || !checkTfaType) {
        steps.fails.add("Match not found: " +  row);
      }
    }
    steps.failHandler();
  }

  @And("^I click Login section "
      + "\"(change password|change method|activate now)\" link on Security page$")
  public void clickLoginChangeLink(String linkType) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click Login section " + linkType + " link on Security page");
    Assert.assertTrue(security.clickOnLoginSection(linkType));
  }
}
