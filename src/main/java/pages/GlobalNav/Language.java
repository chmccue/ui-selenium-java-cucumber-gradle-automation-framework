package pages.GlobalNav;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.AlertModal;

public class Language extends NavMenuBase {

  public Language(WebDriver driver) { super(driver); }

  private final By footerNavLangMenu = strToBy("[class*='language']");

  // menuType can be "footer" or "top nav"
  public boolean clickNavLanguageItem(String item, String menuType) {
    By toClick = textHas(menuType, "footer")
        ? addStrToBy(footerNavLangMenu, " [value*='" + item + "' i]")
        : addStrToBy(menuSelection, "[value*='" + item + "' i]");
    By selectedLang = addStrToBy(toClick, "[selected], [aria-selected='true']");
    scrollScreen(footerNav);
    new AlertModal(driver).acceptCookieAlertIfOpen();
    // if language is already selected, re-selecting it will not refresh/display language in url
    boolean checkIfLangAlreadySelected = exists(selectedLang);
    // "/logout" page redirects to sign-in, and url does not display the language update
    boolean checkIfOnLogout = textHas(currentUrl(), "/logout");
    boolean changeLanguage = selectDropdownOption(footerNavLangMenu, toClick);
    if (checkIfOnLogout || checkIfLangAlreadySelected) return changeLanguage;
    return changeLanguage && regexMatch(currentUrl(), item);
  }
}
