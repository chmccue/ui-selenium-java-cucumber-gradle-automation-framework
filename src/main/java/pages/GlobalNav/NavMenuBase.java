package pages.GlobalNav;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.clickUtils;

public class NavMenuBase extends clickUtils {

  public NavMenuBase(WebDriver driver) { super(driver);}

  private final By primaryNav = By.cssSelector("[class*='Navigation-module_navigation' i]");
  private final By popoverMenu = By.cssSelector(".popover-menu-overlay:not([class*='hidden'])");
  private final By acctSubNav = By.cssSelector("[class*='module_subNavigation' i], .nav-pills");
  private final By orderFormNav = By.cssSelector("#order-form-nav");
  private final By topNavPublic = By.cssSelector(".header nav:not([class*='mobile'])");
  private final By topNavPublicSignUp = By.cssSelector("[class*='module_topBar']");
  final By footerNav = By.cssSelector("footer");

  // Mobile
  private final By mobileNavContent = By.cssSelector(".mobile-nav");

  By navType(String baseNav) throws IllegalArgumentException {
    By nav;
    if (textHas(baseNav, "main account")) {
      nav = primaryNav;
    } else if (textHas(baseNav, "sub account")) {
      nav = acctSubNav;
    } else if (textHas(baseNav, "order form")) {
      nav = orderFormNav;
    } else if (textHas(baseNav, "(user|popover) menu")) {
      nav = popoverMenu;
    } else if (textHas(baseNav, "public top")) {
      // sign up has different top nav than rest of public pages and gets set below
      nav = textHas(currentUrl(), "/sign-up") ? topNavPublicSignUp : topNavPublic;
    } else if (textHas(baseNav, "public mobile")) {
      nav = mobileNavContent;
    } else if (textHas(baseNav, "footer")) {
      nav = footerNav;
    } else {
      String error = "'" + baseNav + "' nav name not a valid argument for navType method";
      Log.error(error);
      throw new IllegalArgumentException(error);
    }
    return nav;
  }

  public By buildNavLocator(String baseNav, String navLink) {
    return combineSelectors(navType(baseNav), setHrefVar(navLink));
  }

  public boolean clickNavLink(String baseNav, String navLink) {
    return clickNavLink(baseNav, navLink, true);
  }

  public boolean clickNavLink(String baseNav, String navLink, boolean checkLinkActive) {
    By clickLink = buildNavLocator(baseNav, navLink);
    if (checkLinkActive) {
      By linkActive = activeNavLink(baseNav, navLink);
      return clickOnAndWait(clickLink, linkActive, 4);
    } else {
      String validateLink = navLink.replaceAll("\\{\\S}", "");
      if (textHas(navLink, "company pro")) {
        navLink = "launch-trade";
        validateLink = "trade.company.com";
      }
      return clickOn(buildNavLocator(baseNav, navLink)) && textHas(currentUrl(), validateLink);
    }
  }

  public By activeNavLink(String baseNav, String subMenu) {
    return activeNavLink(baseNav, subMenu, ".active ");
  }

  private By activeNavLink(String baseNav, String subMenu, String activeElement) {
    return addStrToBy(addStrToBy(navType(baseNav), activeElement), byToStr(setHrefVar(subMenu)));
  }

  boolean hoverAndCheckActiveNavLink(String baseNav, String subNav) {
    return hoverAndWait(cssBuilder(
        "class*", baseNav), activeNavLink("popover menu", subNav, " .active"));
  }
}
