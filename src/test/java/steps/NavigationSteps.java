package steps;

import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import main.Core;
import org.junit.Assert;
import pages.GlobalNav.Footer;
import pages.GlobalNav.Language;

public class NavigationSteps implements Base {

  private final Footer footer = new Footer(driver);

  @Then("^I click \"([^\"]*)\" link on \"([^\"]*)\" nav$")
  public void clickItemInNav(String navName, String navType) {
    if (Core.skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I click " + navName + " link on " + navType + " nav");
    if (textHas(navType, "user ?menu")) {
      Assert.assertTrue(navMenu.clickDropDownLink("_usermenu > " + navName));
    } else if (textHas(navType, "public")) {
      Assert.assertTrue(navMenu.clickPublicTopNavLink(navType, navName));
    } else if (textHas(navType, "footer")) {
      Assert.assertTrue(footer.clickFooterNavItem(navName));
    } else if (textHas(navType, "(main account)")) {
      Assert.assertTrue(navMenu.clickLoggedInPrimaryNav(navName));
    } else {
      Assert.assertTrue(navMenu.clickNavLink(navType, navName));
    }
    Assert.assertTrue(page.pageLoadsWithoutKnownErrors());
  }

  @Then("^I make sure sub sections expand and collapse in user menu$")
  public void userMenuExpandCollapse() {
    Log.info("I make sure sub sections expand and collapse in user menu");
    String userMenu = "_usermenu";
    Assert.assertTrue(navMenu.topMenuExpandCollapseSubSections(userMenu, "expand"));
    Assert.assertTrue(navMenu.topMenuExpandCollapseSubSections(userMenu, "collapse"));
  }

  // Should take the unique value attribute of the language to switch to, such as "en-us"
  @Then("^I select and verify \"([^\"]*)\" option from (footer|top nav) language menu$")
  public void selectFooterLanguage(String langValue, String navType) {
    if (Core.skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I select and verify " + langValue + " option from " + navType + " language menu");
    Assert.assertTrue(new Language(driver).clickNavLanguageItem(langValue, navType));
    Assert.assertFalse(textHas(page.currentUrl(), "(404|500)$"));
  }
}
