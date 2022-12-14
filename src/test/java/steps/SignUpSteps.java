package steps;

import static utils.matcherUtils.textHas;

import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.EnterSite.SignUp;
import java.util.Map;

public class SignUpSteps implements Base {

  private final SignUp signUp = new SignUp(driver);

  // enter "| State | not present |" to validate the State/Province field is not present
  @Then("^I enter the following on (Account Activation|Signup) page:$")
  public void enterInSignUpFields(String signupPage, Map<String, String> params) {
    Log.info("I enter the following on "  + signupPage + " page:\n" + params.toString());
    for (Map.Entry<String, String> item: params.entrySet()) {
      String value = item.getValue() == null ? "" : item.getValue();
      if (textHas(item.getKey(), "(email|key|user|pass)")) {
        Assert.assertTrue(signUp.fillOutSharedFormField(item.getKey(), value));
      } else {
        if (textHas(item.getKey(), "state")
            && textHas(value, "not present")) {
          Log.addStepLog("Checking State/Province field is not present");
          Assert.assertFalse(page.exists(signUp.signUpProvinceFld));
        } else {
          Assert.assertTrue(signUp.fillOutSignupFormData(item.getKey(), value));
        }
      }
    }
  }

  @Then("^I click \"([^\"]*)\" checkbox on Signup page$")
  public void clickOnCheckbox(String item) {
    Log.info("I click " + item + " checkbox on Sign Up page section");
    Assert.assertTrue(signUp.clickSignUpCheckbox(item));
  }

  @Then("^I am on \"([^\"]*)\" Signup page$")
  public void onSignUpPage(String section) {
    Log.info("I am on " + section + " Signup page");
    Assert.assertTrue(signUp.onSignUpPageSection(section));
  }
}
