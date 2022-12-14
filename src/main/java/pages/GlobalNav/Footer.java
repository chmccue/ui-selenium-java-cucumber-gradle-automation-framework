package pages.GlobalNav;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.AlertModal;

public class Footer extends NavMenuBase {

  public Footer(WebDriver driver) {
    super(driver);
  }

  public boolean clickFooterNavItem(String item) {
    new AlertModal(driver).acceptCookieAlertIfOpen();
    By navClick = buildNavLocator("footer", item);
    waitForElement(navClick, 5);
    scrollScreen(navClick);
    return clickOnAndVerifyUrl(navClick, item, false);
  }
}