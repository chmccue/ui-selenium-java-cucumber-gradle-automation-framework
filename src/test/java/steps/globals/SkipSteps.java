package steps.globals;

import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import main.Core;
import main.Log;

public class SkipSteps {

  @Then("^turn on skipStep if \"([^\"]*)\" contains \"([^\"]*)\"$")
  public void skipStepsIfContains(String textGroup, String toCheckFor) {
    Log.info("turn on skipStep if " + textGroup + " contains " + toCheckFor);
    boolean checkSkip = textHas(textGroup, toCheckFor);
    if (checkSkip) Core.skipStep = true;
  }

  @Then("^turn off skipStep")
  public void turnOffSkipStep() {
    Core.skipStep = false;
  }
}
