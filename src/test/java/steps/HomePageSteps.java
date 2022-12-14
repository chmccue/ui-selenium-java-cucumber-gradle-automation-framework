package steps;

import static main.Core.skipStep;

import org.junit.Assert;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import pages.LoggedOutPage;

public class HomePageSteps implements Base {

  private final LoggedOutPage homePage = new LoggedOutPage(driver);

  @Given("^I have an open browser on test site$")
  public void gotoTestSite() throws Exception {
    Log.info("I have an open browser on test site");
    coreEnv.switchToPage("Home");
    Log.info("Home Title: " + page.currentTitle());
    Assert.assertTrue(page.regexMatch(page.currentTitle(), "Test"));
    Assert.assertTrue(navMenu.publicTopNavLoginPresent(true));
  }

  @Given("^I resize to mobile resolution$")
  public void resizeToMobile() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I resize to mobile resolution");
    page.resizeForMobileBrowser();
    page.hardWait(0.2);
  }

  @Then("^I am on the logged out landing page$")
  public void loggedOutLandingPage() {
    Log.info("I am on the logged out landing page");
    Assert.assertTrue(homePage.onLoggedOutLandingPage());
    LoginPageSteps.loggedIn = false;
  }

}
