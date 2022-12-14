package steps;

import static main.Core.skipStep;

import io.cucumber.java.en.And;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.Security.PasswordSetUp;

public class SecuritySetUpSteps implements Base {

  private static final PasswordSetUp security = new PasswordSetUp(driver);

  @And("^I confirm and close tfa settings modal$")
  public void confirmAndCloseSettingsModal() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I confirm and close tfa settings modal");
    Assert.assertTrue(security.confirmAndCloseSettingsModal());
  }

  @And("^I submit correct authenticator code$")
  public void enterAndSubmitAuthCode() {
    Log.info("I submit correct authenticator code");
    Assert.assertTrue(security.getAndEnterAuthKey());
  }
}
