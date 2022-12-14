package steps.globals;

import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.EnterSite.LoginPage;

public class SharedLoginSignUpSteps implements Base {

  private final LoginPage enterSite = new LoginPage(driver);

  @Then("^I click submit button on page$")
  public void clickSubmit() {
    Log.info("I click submit button on page");
    Assert.assertTrue(enterSite.clickSubmitBtn());
  }

  @Then("^I confirm submit button (enabled|disabled) on page$")
  public void confirmEnterSiteSubmitBtnStatus(String btnStatus) {
    Log.info("I confirm submit button " + btnStatus + " on page");
    Assert.assertEquals(textHas(btnStatus,"enabled"), page.enabled(enterSite.submitBtn));
  }
}
