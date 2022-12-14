package pages.Globals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.matcherUtils;

public class ErrorMsg extends matcherUtils {

  public ErrorMsg(WebDriver driver) { super(driver); }

  private final By nonFocusedErrorMsg = By.cssSelector(".hide-when-predecessor-has-focus");

  public boolean checkErrorMessage(String errorText, String foundOrNot) {
    boolean found = regexElementMatch(nonFocusedErrorMsg, errorText);
    return (textHas(foundOrNot, "^found$") && found)
        || (textHas(foundOrNot, "not found") && !found);
  }
}
