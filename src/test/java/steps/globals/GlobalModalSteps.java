package steps.globals;

import static main.Core.skipStep;
import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;

public class GlobalModalSteps implements Base {

  @Then("^I click modal button \"([^\"]*)\"$")
  public void clickModalBtn(String item) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click modal button " + item);
    Assert.assertTrue(page.globalClickModalBtn(item));
  }

  // TODO-for ChristopherMC:
  // In StakingActions.feature L17, L19 `I confirm modal button "Stake funds" as status "disabled"`
  // is added and it might be convenient to accommodate that button coverage
  // in this step method. Button name is "Stake funds"
  @Then("^I confirm modal button \"([^\"]*)\" as status \"([^\"]*)\"$")
  public void confirmModalBtnStatus(String item, String enabledOrDisabled) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I confirm modal button " + item + " as status " + enabledOrDisabled);
    if (enabledOrDisabled.equalsIgnoreCase("disabled")) {
      Assert.assertTrue(page.globalModalBtnDisabled(item));
    } else {
      Assert.assertFalse(page.globalModalBtnDisabled(item));
    }
  }

  @Then("^I close modal with Escape key$")
  public void closeModalWithEscape() {
    Log.info("I close modal with Escape key");
    Assert.assertTrue(page.closeModalWithEscapeKey(true));
  }

  @Then("^I verify modal is \"(found|still found|not found)\"$")
  public void modalPresent(String foundOrNot) {
    Log.info("I verify modal is " + foundOrNot);
    Assert.assertTrue(page.modalVisibleStatus(foundOrNot));
  }

  @Then("^I verify facade modal is found$")
  public void modalFacadePresent() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I verify facade modal is found");
    Assert.assertTrue(page.modalVisibleStatus("found", page.globalModalContentFacadeOnly));
  }

  @Then("^I close facade modal$")
  public void modalFacadeClose() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I close facade modal");
    page.pressEscBtn();
    Assert.assertTrue(page.modalVisibleStatus("not found", page.globalModalContentFacadeOnly));
  }

  @Then("^I count \"([^\"]*)\" checked checkbox(?:|es) (found|not found) in modal$")
  public void modalCheckboxCount(String count, String foundOrNot) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    if (count == null || count.isEmpty()) return;
    Log.info("I count " + count + " checked checkboxes " + foundOrNot + " in modal");
    if (textHas(foundOrNot, "not found")) {
      Assert.assertNotEquals(Integer.parseInt(count), page.getCount(page.globalModalBoxChecked));
    } else {
      Assert.assertEquals(Integer.parseInt(count), page.getCount(page.globalModalBoxChecked));
    }
  }

  @Then("^I should see \"([^\"]*)\" invalid modal field(?:|s)$")
  @Then("^I should see \"([^\"]*)\" invalid field(?:|s)$")
  public void modalCountInvalidFields(int itemCount) {
    Log.info("I should see " + itemCount + " invalid modal fields");
    Assert.assertEquals(itemCount, page.getCount(page.invalidField, 2));
  }

  @Then("^I click the following checkboxes in modal:$")
  public void clickCheckBox(String enteredCheckBoxes) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    if (enteredCheckBoxes == null || enteredCheckBoxes.length() == 0) return;
    Log.info("I click the following checkboxes in modal: " + enteredCheckBoxes);
    Assert.assertTrue(page.clickCheckBoxes(enteredCheckBoxes));
  }

  // works when there is a single input field in modal.
  @Then("^I enter \"([^\"]*)\" in modal field$")
  public void modalEnterInMainField(String text) {
    Log.info("I enter " + text + " in modal field");
    Assert.assertTrue(page.clearAndEnterText(page.globalModalField, text));
  }

  @Then("^I click away from modal field$")
  @Then("^I click away from field$")
  public void modalClickAwayFromField() {
    Log.info("I click away from field");
    Assert.assertTrue(page.clickOn(page.globalModalTitle));
  }
}