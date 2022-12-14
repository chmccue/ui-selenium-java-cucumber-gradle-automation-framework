package steps.globals;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.Globals.ErrorMsg;

public class ErrorMsgSteps implements Base {

  // takes error message text string and asserts it is found or not.
  @Then("^I confirm \"([^\"]*)\" error message (found|not found)$")
  public void checkErrorMessage(String errorText, String foundOrNot) {
    Log.info("I confirm '" + errorText + "' error message " + foundOrNot);
    if (!new ErrorMsg(driver).checkErrorMessage(errorText, foundOrNot)) {
      Assert.fail("Expected '"
          + errorText + "' error message to be " + foundOrNot + ", but opposite was true");
    }
  }
}
