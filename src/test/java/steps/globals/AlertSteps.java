package steps.globals;

import static main.Core.skipStep;
import static pages.Globals.AlertModal.globalAlertFail;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;

public class AlertSteps implements Base {

  @Then("^I confirm \"([^\"]*)\" alert$")
  public void checkUpdateMessage(String status) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I confirm '" + status + "' alert");
    Assert.assertTrue(page.checkAlertTypeAndMessage(page.strToLst(status)));
  }

  @Then("^I confirm no error alert$")
  public void noErrorAlert() {
    Log.info("I confirm no error alert");
    Assert.assertTrue(page.waitForNotVisible(globalAlertFail, 4));
  }

  @Then("^I confirm \"([^\"]*)\" alert and close alert message$")
  public void checkUpdateMessageAndClose(String status) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I confirm '" + status + "' alert and close alert message");
    Assert.assertTrue(page.checkAlertTypeAndMessage(page.strToLst(status)));
    Assert.assertTrue(page.closeAlertMessage());
  }

  @Then("^I accept cookie alert$")
  public void acceptCookieAlert() {
    Log.info("I accept cookie alert");
    Assert.assertTrue(page.acceptCookieAlertIfOpen());
  }
}
