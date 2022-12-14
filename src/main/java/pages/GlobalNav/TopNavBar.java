package pages.GlobalNav;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.AlertModal;

public class TopNavBar extends NavMenuBase {

  public TopNavBar(WebDriver driver) {
    super(driver);
  }

  private final AlertModal alertModal = new AlertModal(driver);
  // Mobile
  private final By mobileNavButton = By.cssSelector(".hamburger");
  private final By mobileNavIsOpen = By.cssSelector(".mobile-nav-container.open");
  private final By mobileNavSubNavTrigger = By.cssSelector(".subnav-trigger");

  private final By topMenuSubExpandOpen = strToBy(
      "[class*='user' i][aria-expanded='true'], .expandable-menu-item .fa-caret-up");
  private final By topMenuSubExpandClosed = strToBy(
      "[class*='user' i][aria-expanded='false'], .expandable-menu-item .fa-caret-down");

  private final By launchPadDismissBtn = strToBy(
      "[class*='launchpad-module' i] [class*='dismiss' i]");
  public final String topNavFeatures = "[data-testid*='nav'][data-testid*='Features' i]";

  // If "openNav" true, opens the nav. If false, closes it.
  public boolean openOrCloseMobileNav(boolean openNav) {
    if (openNav) {
      if (!exists(mobileNavIsOpen)) {
        return clickOnAndWait(mobileNavButton, mobileNavIsOpen, 2);
      }
    } else {
      if (exists(mobileNavIsOpen)) {
        return clickOn(mobileNavButton) && waitForNotVisible(mobileNavIsOpen, 1);
      }
    }
    return true;
  }

  public boolean publicTopNavLoginPresent(boolean retry) {
    By signInLink = buildNavLocator("public top", "sign-in");
    boolean checkNav = waitForElement(signInLink, 5);
    if (!checkNav && retry) {
      driver.get(currentUrl());
      return publicTopNavLoginPresent(false); //refreshes and reruns if fails, due to existing bug.
    }
    return checkNav;
  }

  public boolean clickPublicTopNavLink(String navType, String navName) {
    alertModal.acceptCookieAlertIfOpen(); // pop up can interfere with click
    boolean adjustSubNav = true;
    String[] navNames = navName.split(" ?> ?");
    navName = navNames[navNames.length - 1];
    if (textHas(navName, "login")) navName = "sign-in";
    if (textHas(navName, "signup")) navName = "sign-up";
    if (textHas(navType, "mobile")) {
      adjustSubNav = expandOrClosePublicMobileSubNav(navNames[0], navNames[navNames.length - 1]);
      if (navNames.length == 1 || textHas(navName, "(collapse|expand)")) {
        return adjustSubNav;
      }
    } else if (navNames.length > 1) {
      hover(buildNavLocator("public top", navNames[0]));
    }
    By toClick = buildNavLocator(navType, navName);
    scrollScreen(toClick);
    return adjustSubNav && clickOnAndVerifyUrl(toClick, navName, false);
  }

  public boolean expandOrClosePublicMobileSubNav(String navName, String subNavName) {
    openOrCloseMobileNav(true);
    if (textHas(navName, "(features|learn)")) {
      By mobileSubNav = addStrToBy(
          mobileNavSubNavTrigger, "[data-testid*='" + navName + "' i]");
      By mobileSubNavOpen = strToBy(".open > " + byToStr(mobileSubNav));
      if (textHas(subNavName, "collapse")) {
        return clickOn(mobileSubNav) && waitForNotVisible(mobileSubNavOpen, 2);
      } else {
        return clickOnAndWait(mobileSubNav, mobileSubNavOpen, 2);
      }
    }
    return true;
  }

  public boolean clickLoggedInPrimaryNav(String navName) {
    String[] navNameList = navName.replaceAll("[ }]", "").split(">");
    alertModal.closeAlertMessage();
    if (navNameList.length > 1) return clickDropDownLink(navName);
    By toClick = buildNavLocator("main account", navName);
    String validateUrl = navNameList[0].split("\\{")[0];
    if (textHas(navName, "markets")) validateUrl = "trade.company.com";
    return waitForElement(toClick, 2)
        && clickOnAndVerifyUrl(toClick, validateUrl, false);
  }

  // Takes a string with 2 or 3 values separated by '>', such as 'classNameNav > hrefSubNavLink'
  public boolean clickDropDownLink(String menuPath) {
    String[] menuPathList = menuPath.replaceAll(" ", "").split(">");
    By menuTrigger = cssBuilder("class*", menuPathList[0]);
    menuPath = menuPathList[menuPathList.length - 1];
    waitForElement(menuTrigger, 3);
    By clickLink = buildNavLocator("popover menu", menuPath);
    openDropdownMenu(menuTrigger);
    if (textHas(menuPath, "log ?out")) {
      return clickOnAndVerifyUrl(clickLink, menuPath.split("\\{")[0], false);
    }
    if (menuPathList.length > 2) {
      if (!topMenuExpandCollapseSubSections(menuPathList[0], "expand")) return false;
    }
    boolean checkLinkClick = clickOn(clickLink) && waitForPageReload(2);
    alertModal.closeAlertMessage();
    boolean checkAfterClick;
    checkAfterClick = waitForNotVisible(navType("popover menu"), 2);
    return checkLinkClick && checkAfterClick;
  }

  public boolean topMenuExpandCollapseSubSections(String menuClass, String expandOrCollapse) {
    By toClick = expandOrCollapse.equalsIgnoreCase("collapse")
        ? topMenuSubExpandOpen : topMenuSubExpandClosed;
    By toBeVisible = expandOrCollapse.equalsIgnoreCase("collapse")
        ? topMenuSubExpandClosed : topMenuSubExpandOpen;
    By menuTrigger = cssBuilder("class*", menuClass);
    if (!openDropdownMenu(menuTrigger)) return false;
    int retry = 5;
    while (exists(toClick) && retry > 0) {
      retry -= 1;
      try {
        clickOn(toClick);
      } catch (Exception e) {
        Log.error(e.toString());
        openDropdownMenu(menuTrigger);
      }
    }
    boolean menuReady = waitForNotVisible(toClick, 1) && waitForElement(toBeVisible, 2);
    hardWait(0.2);
    return menuReady;
  }

  private boolean openDropdownMenu(By menu) {
    if (!exists(navType("user menu"))) clickOn(menu);
    return waitForElement(navType("popover menu"), 1);
  }

  public boolean dismissLaunchPadToolTip() {
    if (waitForElement(launchPadDismissBtn, 0.1)) {
      return clickOn(launchPadDismissBtn) && waitForNotVisible(launchPadDismissBtn, 2);
    }
    return true;
  }
}
