package steps.globals;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;

public class ToolTipSteps implements Base {

  @Then("^I confirm \"([^\"]*)\" tooltip$")
  public void checkToolTipMessage(String status) {
    Log.info("I confirm '" + status + "' tooltip");
    Assert.assertTrue(page.checkRCToolTipMessage(page.strToLst(status)));
  }

}
