package steps;

import static utils.matcherUtils.textHas;

import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import main.Core;
import org.junit.Assert;
import pages.Security.Api;
import steps.globals.AlertSteps;
import steps.globals.MiscSteps;

public class SecurityApiSteps implements Base {

  private final Api api = new Api(driver);
  private final NavigationSteps navSteps = new NavigationSteps();
  private final MiscSteps miscSteps = new MiscSteps();
  static String EnteredApiKeyName;

  @After(value = "@teardown-delete-api-keys", order = 1)
  public void teardownDeleteApiKeys() {
    Log.info("teardown: check for and delete API keys");
    if (!textHas(page.currentUrl(), "/api$")) {
      page.pageRefresh();
      navSteps.clickItemInNav("Security > API", "user menu");
    }
    Assert.assertTrue(api.deleteAllApiKeys());
  }

  @Then("^I go and create new API Key with name \"([^\"]*)\"$")
  public void goToApiAndCreateApiKey(String keyName) {
    if (Core.skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I go and create new API Key with name " + keyName);
    if (!textHas(page.currentUrl(), "/api$")) {
      page.pageRefresh();
      navSteps.clickItemInNav("Security > API", "user menu");
    }
    clickAddKey("link");
    setApiFieldTo(keyName, "Key Name");
    checkApiPermissions("Query Funds, Modify Orders");
    miscSteps.clickOnPageText("button::generate key");
    new AlertSteps().checkUpdateMessageAndClose("success, Created API key");
    miscSteps.clickOnPageText("button::save");
  }

  @Then("^I check the \"(.*)\" API key permissions$")
  public void checkApiPermissions(String permissions) {
    Log.info("I check the " + permissions + " key permissions");
    for (String cbox : permissions.split(", ?")) {
      if (!page.clickOnText("[for]:not([class*='field' i])", cbox)) {
        steps.fails.add("Error finding or clicking on checkbox " + cbox);
      }
    }
    steps.failHandler();
  }

  @Then("^I click on add key (button|link)$")
  public void clickAddKey(String buttonOrLink) {
    Log.info("I click on add key " + buttonOrLink);
    if (textHas(buttonOrLink, "button")) {
      Assert.assertTrue(page.clickOn(api.apiAddKeyTableBtn));
    } else {
      miscSteps.clickOnPageText("add key");
    }
  }

  @Then("^I toggle the \"([^\"]*)\" switch (on|off) in API Settings$")
  public void clickApiSwitch(String toggleName, String onOrOff) {
    Log.info("I toggle the '" + toggleName + "' switch " + onOrOff + " in API Settings");
    Assert.assertTrue(api.toggleApiSwitch(toggleName, onOrOff));
  }

  @Then("^I set the calendar date for \"([^\"]*)\" switch in API Settings$")
  public void selectApiSwitchDate(String toggleName) {
    Log.info("I set the calendar date for '" + toggleName + "' switch in API Settings");
    Assert.assertTrue(api.apiSwitchOpenCalendar(toggleName));
    Assert.assertTrue(api.selectCalendarDateTime());
    Assert.assertTrue(api.apiCalendarFieldNotEmpty(toggleName));
  }

  @Then("^I enter \"([^\"]*)\" in \"([^\"]*)\" API field$")
  public void setApiFieldTo(String value, String field) {
    Log.info("I enter " + value + " in " + field + " API field");
    EnteredApiKeyName = value;
    Assert.assertTrue(api.enterApiFieldText(field, value));
  }

  @Then("^I (edit|delete) the \"([^\"]*)\" API Key$")
  public void editDeleteApiKey(String editOrDelete, String keyName) {
    Log.info("I " + editOrDelete + " the " + keyName + " API Key");
    Assert.assertTrue(textHas(editOrDelete, "delete")
        ? api.deleteApiKey(keyName) : api.editApiKey(keyName));
  }
}
