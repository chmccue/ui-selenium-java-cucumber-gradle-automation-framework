package steps.globals;

import static main.Core.skipStep;
import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import main.Base;
import main.Core;
import main.Log;
import org.junit.Assert;
import org.openqa.selenium.By;
import pages.Globals.Loading;
import java.util.List;

public class MiscSteps implements Base {
  
  @Then("^I enter text \"([^\"]*)\" in field \"([^\"]*)\"$")
  public void enterTextInField(String text, String fieldLocator) {
    Log.info("I enter text " + text + " in field "  + fieldLocator);
    Assert.assertTrue(page.enterText(page.strToBy(fieldLocator), text));
  }

  // Will traverse page for link text and click on it if found.
  // To add specific locator for text search, add locator separated with "::". locator::text
  @Then("^I click on text \"([^\"]*)\"$")
  public void clickOnPageText(String textToClick) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    page.hardWait(0.1);
    Log.info("I click on text " + textToClick);
    String[] locatorText = textToClick.split("::");
    String locator = locatorText.length > 1 ? locatorText[0] : "body [href]";

    if (textHas(locator, "^modal$")) {
      locator = page.byToStr(page.globalModalContent);
    } else if (textHas(locator, "^modal title$")) {
      locator = page.byToStr(page.globalModalHeader);
    }

    String textLink = locatorText.length > 1 ? locatorText[1] : locatorText[0];
    Assert.assertTrue(page.clickOnText(locator, textLink));
    Assert.assertTrue(new Loading(driver).waitForLoadSpinnerToDisappear(true));
  }

  // See comments in method "clickOnSpecificTextAndVerify" for further details
  @Then("^I click on text, verify and go back:$")
  public void clickOnTextAndVerify(DataTable dt) {
    clickOnSpecificTextAndVerify("body [href]", dt);
  }

  // Will traverse page for link text and click on it if found.
  // Can optionally use a header at top (recommended for understanding tables in feature files)
  // Enter args on feature file as: | Link Text | After Click Url or Title |
  // "locator" arg is css locator to search for the text with. You can also add a locator override
  // by table row (helpful if there are a lot of rows) using "::" separator. Example: locator::text
  @Then("^I click on text in \"([^\"]*)\", verify and go back:$")
  public void clickOnSpecificTextAndVerify(String locator, DataTable dt) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click on text in " + locator + ", verify and go back:\n" + dt);
    List<List<String>> rows = dt.asLists(String.class);
    for (List<String> row : rows) {
      String linkText = row.get(0);
      // skip row if first column equals "linktext". Useful to clarify header name columns
      if (linkText.replace(" ", "").equalsIgnoreCase("linktext")) continue;
      String[] locatorText = linkText.split("::");
      // if custom locator entered in data row, it overrides default locator entered
      locator = locatorText.length > 1 ? locatorText[0] : locator;
      String textLink = locatorText.length > 1 ? locatorText[1] : locatorText[0];
      // if 2nd column is not entered, takes 1st column and uses that as url click through check
      String url = row.size() == 1 || row.get(1) == null ? linkText : row.get(1);
      page.pageLoadsWithoutKnownErrors();
      Log.info("Clicking '" + linkText + "' with validator '" + url + "'");
      if (!page.clickOnTextAndVerifyUrl(locator, textLink, url, true)) {
        steps.fails.add("row data: " + locator + ": " + textLink + ", " + url);
      }
      Runtime.getRuntime().gc();
    }
    steps.failHandler();
  }

  @Then("^I click on \"([^\"]*)\" and verify element(?:|s):$")
  public void clickOnAndVerifyStep(String locator, DataTable data) {
    clickOnStep(locator);
    verifyElementStep("found", data);
  }

  // Can enter optional arguments: if after link click is different from href, separate with comma:
  // hrefName=prices, markets (will click on href "prices" and validate "markets" in url).
  @Then("^I click on href \"([^\"]*)\"$")
  public void clickOnAndVerifyHrefStep(String hrefName) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click on href " + hrefName);
    String[] hrefList = hrefName.split(", ?");
    String verifyUrl = hrefList.length > 1 ? hrefList[1] : hrefName;
    Assert.assertTrue(page.clickOnAndVerifyUrl(page.setHrefVar(hrefName), verifyUrl, false));
  }

  @Then("^I click on href, verify and go back:$")
  public void clickOnAndVerifyHrefMulti(DataTable dt) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click on href, verify and go back:\n" + dt);
    List<List<String>> rows = dt.asLists(String.class);
    for (List<String> row : rows) {
      String linkHref = row.get(0);
      String url = row.size() == 1 || row.get(1) == null ? linkHref : row.get(1);
      Log.info("Clicking href '" + linkHref + "' with validator '" + url + "'");
      if (!page.clickOnAndVerifyUrl(page.setHrefVar(linkHref), url, true)) {
        steps.fails.add("clicked on href '" + linkHref + "', expected url '" + url + "'");
      }
      Runtime.getRuntime().gc();
    }
    steps.failHandler();
  }

  // Enter as css or xpath locator: ".myClass" or "//*[@class='myClass']"
  @Then("^I click on \"([^\"]*)\"$")
  public void clickOnStep(String locator) {
    Log.info("I click on " + locator);
    Assert.assertTrue(page.clickOn(page.strToBy(locator)));
  }

  @Then("^I verify elements? (found|not found):$")
  public void verifyElementStep(String foundOrNot, DataTable data) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I verify element(s) " + foundOrNot + ":\n" + data);
    double waitTime = 10;
    boolean firstLocator = true;
    for (String item : page.dtToList(data)) {
      if (item.isEmpty()) continue;
      String[] locatorList = item.split(", ?");
      By locator = page.strToBy(item);
      if (locatorList.length > 1) {
        locator = page.cssBuilder(locatorList[1], locatorList[0]);
      }
      boolean elementCheck = foundOrNot.equals("found")
          ? page.waitForElement(locator, waitTime) : page.waitForNotVisible(locator, waitTime);

      if (!elementCheck) {
        steps.fails.add(item + " should be '" + foundOrNot + "' in " + waitTime + " second(s)");
      }
      if (firstLocator) {
        firstLocator = false;
        waitTime = 1;
      }
    }
    steps.failHandler();
  }

  // Will search for text on entire page (html body)
  @Then("^I verify text (found|not found):$")
  public void verifyTextEntirePageStep(String foundOrNot, DataTable data) {
    verifyTextStep(foundOrNot, "body", data);
  }

  // For specific areas of page to search for text, enter unique locator into locator argument
  // can enter locator="modal" if you want to check text only in modal
  @Then("^I verify text (found|not found) in \"([^\"]*)\":$")
  public void verifyTextStep(String foundOrNot, String locator, DataTable data) {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    if (textHas(locator, "^modal$")) {
      locator = page.byToStr(page.globalModalContent);
    } else if (textHas(locator, "^modal title$")) {
      locator = page.byToStr(page.globalModalHeader);
    }
    Log.info("I verify text " + foundOrNot + " in " + locator + ":\n" + data);
    page.hardWait(0.2);
    page.waitForElement(page.strToBy(locator), 5);
    if (!page.regexElementMatch(
        page.strToBy(locator), page.dtToList(data), foundOrNot.equals("found"))) {
      steps.fails.add("See Output");
    }
    steps.failHandler();
  }

  @Then("^scroll to bottom of page$")
  public void scrollToBottom() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("scroll to bottom of page");
    page.scrollScreenToBottom();
  }

  @Then("^I go to url \"([^\"]*)\"$")
  public void goToDirect(String url) throws Exception {
    coreEnv.open(page.combineSiteUrlAndRelativeUrl(url), 0, true);
  }

  @Then("^I hover over \"([^\"]*)\"$")
  public void hoverOver(String locator) {
    // If locator has ".", "#" or "[", we assume a locator string has been passed in.
    // Otherwise we turn it into an href locator.
    if (textHas(locator, "^top nav features$")) locator = navMenu.topNavFeatures;
    By locatorReady = textHas(locator, "(\\.|#|\\[)")
        ? page.strToBy(locator) : page.setHrefVar(locator);
    Log.info("I hover over " + page.byToStr(locatorReady));
    page.hover(locatorReady);
    if (Core.firefoxTest) page.hardWait(0.2);
  }

  @Then("^I verify I am on url \"([^\"]*)\"$")
  public void verifyIAmOnUrl(String expectedUrl) {
    Log.info("I verify I am on url " + expectedUrl);
    Assert.assertTrue(textHas(page.currentUrl(), expectedUrl));
  }
}
