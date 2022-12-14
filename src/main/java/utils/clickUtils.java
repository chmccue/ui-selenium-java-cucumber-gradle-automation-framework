package utils;

import static main.Base.coreEnv;
import static main.Core.firefoxTest;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ElementNotInteractableException;

public class clickUtils extends matcherUtils {

  protected clickUtils(WebDriver driver) { super(driver); }

  // Click on By locator and return true if successful
  // timeout defaults to 1 second. If longer wait time needed, use clickOn with waitTime argument.
  public boolean clickOn(By locator) {
    return clickOn(locator, 1);
  }

  public boolean clickOn(By locator, double waitTime) {
    return clickOn(locator, waitTime, true);
  }

  public boolean clickOn(By locator, double waitTime, boolean retry) {
    waitForElement(locator, waitTime);
    if (!displayed(locator)) scrollScreen(locator);
    boolean click = clickOn(driver.findElement(locator));
    if (!click && retry) {
      //scroll to top and retry
      Log.warn("clickOn error with " + locator + ".  Attempting retry...");
      ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0)");
      return clickOn(locator, waitTime, false);
    }
    return click;
  }

  // Click on WebElement locator and return true if successful
  // Checks if running with chrome mobile emulator and uses JS click if needed
  public boolean clickOn(WebElement locator) {
    return clickOn(locator, 2);
  }

  private boolean clickOn(WebElement locator, int retry) {
    try {
      locator.click();
      return true;
    } catch (ElementNotInteractableException e) {
      if (retry > 0) {
        ((JavascriptExecutor) driver).executeScript("scrollTo(0, (scrollY+100));");
        hardWait(0.1);
        if (retry == 1 && firefoxTest) return clickJS(locator);
        return clickOn(locator, retry - 1);
      } else {
        Log.error(e.toString());
      }
    } catch (Exception e) {
      Log.error("Exception encountered during click event: " + e);
    }
    return false;
  }

  // Passive method that Will only attempt to click on item if it's found to exist on page.
  public boolean clickOnIfExists(By locator) {
    if (exists(locator)) return clickOn(locator);
    return true;
  }

  // Click on locator using javascript and return true if successful
  private boolean clickJS(WebElement locator) {
    Log.info("Click using JS");
    String result = (String) ((JavascriptExecutor) driver)
        .executeScript("return arguments[0].click()", locator);
    return result == null;
  }

  // Click on By locator and wait for "waitLocator" to be displayed
  public boolean clickOnAndWait(By locator, By waitLocator, double waitTime) {
    return clickOnAndWait(locator, waitLocator, waitTime, 0);
  }

  // If initial check returns false, will retry x amount entered in retryCount until retryCount=0.
  public boolean clickOnAndWait(By locator, By waitLocator, double waitTime, int retryCount) {
    if (clickOn(locator) && waitForElement(waitLocator, waitTime) && displayed(waitLocator)) {
      return true;
    }
    if (retryCount > 0) {
      retryCount -= 1;
      return clickOnAndWait(locator, waitLocator, waitTime, retryCount);
    }
    return false;
  }

  // Clicks random element from locator count. numStart sets minimum number to return randomly.
  protected boolean clickOnRandom(By locator, int numStart) {
    return clickOn(getRandomElement(locator, numStart));
  }

  // Takes cssBase and searches for text match within locator.
  // To search across entire page, enter cssBase="body".
  public boolean clickOnText(String cssBase, String clickText) {
    return clickOnText(cssBase, clickText, true);
  }

  public boolean clickOnText(String cssBase, String clickText, boolean retry) {
    WebElement item = getElementMatch(By.cssSelector(cssBase), clickText);
    if (item == null && retry) {
      hardWait(1);
      return clickOnText(cssBase, clickText, false);
    } else if (item == null) {
      return false;
    }
    return clickOn(item);
  }

  public boolean clickOnTextAndVerifyUrl(
      String cssBase, String clickText, String expectedUrl, boolean pageBack) {
    WebElement item = getElementMatch(By.cssSelector(cssBase), clickText);
    if (item == null) return false;
    return clickOnAndVerifyUrl(item, expectedUrl, pageBack);
  }

  public boolean clickOnAndVerifyUrl(By toClick, String titleOrUrl) {
    return clickOnAndVerifyUrl(driver.findElement(toClick), titleOrUrl, true);
  }

  public boolean clickOnAndVerifyUrl(By toClick, String titleOrUrl, boolean pageBack) {
    return clickOnAndVerifyUrl(driver.findElement(toClick), titleOrUrl, pageBack);
  }

  public boolean clickOnAndVerifyUrl(WebElement toClick, String titleOrUrl, boolean pageBack) {
    return clickOnAndVerifyUrl(toClick, titleOrUrl, pageBack, true);
  }
  
  // Designed to check page link and validate url or title of opened page.
  // If page opens in new tab/window, closes new page and returns to start page.
  // If page opens in same tab, use boolean `pageBack` to return to start page or not.
  private boolean clickOnAndVerifyUrl(
      WebElement toClick, String titleOrUrl, boolean pageBack, boolean retry) {
    String mainWindow = currentWindowId(); // Save WindowHandle of Parent Browser Window
    String startUrl = currentUrl();
    if (!clickOn(toClick)) return false;
    hardWait(0.8); // minimal delay input during page load.
    try {
      boolean checkForExpectedWindow = switchWindowControl(titleOrUrl, 3);
      coreEnv.closeAllTabsExcept(mainWindow);
      if (!startUrl.equals(currentUrl()) && driver.getWindowHandles().size() == 1 && pageBack) {
        pageBack();
      }
      return checkForExpectedWindow;
    } catch (org.openqa.selenium.WebDriverException e) {
      Log.error(e.toString());
      if (retry) return clickOnAndVerifyUrl(toClick, titleOrUrl, pageBack, false);
    }
    return false;
  }

  //////////////////////////////////////////////////
  // selectDropdown methods are clickOn sub methods
  //////////////////////////////////////////////////

  private final By menuSearchFld = strToBy("input[aria-autocomplete='list'][aria-controls]");
  private final String menuItem = " option, .Select-option, [aria-hidden='false'] li.pointer";
  public final By menuSelection = strToBy("[role='listbox']:not([hidden]) [role*='option']");

  // Opens selector and clicks option. Returns true if click succeeds.
  public boolean selectDropdownOption(By menu, By option) {
    clickOnAndWait(menu, option, 3);
    scrollScreen(option); // In case menu option is off screen, scrolls it into view.
    return clickOn(option);
  }

  public boolean selectDropdownOption(By menu, String textOption) {
    boolean openMenu = clickOn(menu);
    String menuItemLocal = byToStr(menu) + menuItem;
    hardWait(0.1);
    return openMenu && clickOnText(menuItemLocal, textOption);
  }

  protected boolean selectRandomDropdownOption(By menu) {
    return clickOn(menu) && clickOnRandom(strToBy(byToStr(menu) + " " + menuItem), 1);
  }

  // to search for end of string, enter "$" at end of string. For example: "ethereum$"
  private boolean searchAndSelectDropDownMenu(By menu, String searchText) {
    String searchTextClean = searchText.replaceAll("\\$$", "");
    return clickOn(menu) && clearAndEnterText(menuSearchFld, searchTextClean)
        && clickOnText(menuItem, searchText) && waitForNotVisible(strToBy(menuItem), 1);
  }

  public boolean selectDropDown(By formFld, String input) {
    if (elementHas(formFld, input)) return true;
    return textHas(input, "(random|any)")
        ? selectRandomDropdownOption(formFld) : searchAndSelectDropDownMenu(formFld, input);
  }
}
