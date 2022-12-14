package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.clickUtils;

public class LoggedOutPage extends clickUtils {

  public LoggedOutPage(WebDriver driver) { super(driver); }

  private final By loggedOutPageContent  = By.cssSelector("#logout");

  public boolean onLoggedOutLandingPage() {
    return waitForElement(loggedOutPageContent, 8);
  }
}