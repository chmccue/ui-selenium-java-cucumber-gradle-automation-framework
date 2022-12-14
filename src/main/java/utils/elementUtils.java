package utils;

import main.Driver;
import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

public class elementUtils extends locatorBuilder {

  protected elementUtils(WebDriver driver) { super(driver); }

  // Checks is element exists without throwing exception
  public boolean exists(By locator) {
    return (driver.findElements(locator).size() > 0);
  }

  public boolean exists(WebElement element) {
    return exists((By) element);
  }

  // Checks if element is enabled
  // Validates for 2 possible disabled triggers, as they are both in use on the site
  public boolean enabled(By locator) {
    boolean enabledCheck = driver.findElement(locator).isEnabled();
    if (enabledCheck && !byToStr(locator).contains("//")) {
      for (By loc : separateCombinedCss(locator)) {
        enabledCheck = !exists(addStrToBy(loc, ".disabled, [aria-disabled='true']"));
        if (!enabledCheck) break;
      }
    }
    return enabledCheck;
  }

  // takes a By locator, and if it has multiple locators separated by commas, will separate them
  // and return them separated within a List.
  private List<By> separateCombinedCss(By locator) {
    List<By> locators = new ArrayList<>();
    for (String s : byToStr(locator).split(", ")) {
      locators.add(strToBy(s));
    }
   return locators;
  }

  protected boolean displayed(By locator) {
    try {
      return driver.findElement(locator).isDisplayed();
    } catch (Exception e) {
      Log.error(e.toString());
      return false;
    }
  }

  // Scrolls screen to put "locator" into view on page
  protected void scrollScreen(By locator) {
    try {
      waitForElement(locator, 5);
      ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();",
          driver.findElement(locator));
      hardWait(0.1);
    } catch (WebDriverException e) {
      Log.warn("Scroll did not execute as expected. " + e.toString());
    }
  }

  // Wait to be visible and clickable. Use when expected result is true.
  // If defined, this method will use max wait seconds variable to overwrite set wait time.
  public boolean waitForElement(By locator, double timeout) {
    return waitForElement(locator, Math.max(timeout, Driver.maxWaitSeconds), true);
  }

  // Wait to be visible and clickable. logger=false; use when expected result is not always true.
  protected boolean waitForElement(By locator, double timeout, boolean logger) {
    return waitForElement(locator, timeout, logger, 0);
  }

  public boolean waitForElement(By locator, double timeout, boolean logger, int retry) {
    try {
      wait(timeout).until(ExpectedConditions.and(ExpectedConditions
          .visibilityOfElementLocated(locator), ExpectedConditions.elementToBeClickable(locator)));
    } catch (Exception e) {
      if (logger) {
        Log.warn("Locator '" + locator.toString() + "' not found in '" + timeout
            + "' second(s) on page " + currentUrl());
        Log.warn("Exception caught during wait: " + e.toString());
      }
      if (retry > 0) {
        retry -= 1;
        Log.warn("Reloading page and rechecking");
        pageRefresh();
        return waitForElement(locator, timeout, true, retry);
      }
      return false;
    }
    return true;
  }

  protected boolean waitForElements(Set<By> locators) {
    return waitForElements(locators, 2, true, true);
  }

  protected boolean waitForElements(
      Set<By> locators, double timeout, boolean shouldBePresent) {
    return waitForElements(locators, timeout, shouldBePresent, true);
  }

  // HashSet<By> locators: multiple By locators to validate in single method.
  // double timeout: amount in seconds to wait for each locator.
  // boolean shouldBePresent: if true, checks locators are visible. If false, checks not visible.
  protected boolean waitForElements(
      Set<By> locators, double timeout, boolean shouldBePresent, boolean reportLog) {
    boolean removeTimeoutAfterFirstCheck = true;
    for (Iterator<By> getElement = locators.iterator(); getElement.hasNext();) {
      By locator = getElement.next();
      Log.info("Checking Should Be Present=" + shouldBePresent + ": `" + locator.toString()
          + "` on " + currentUrl() + " in " + timeout + " second(s)");
      boolean toCheck = shouldBePresent ?
          waitForElement(locator, timeout) : waitForNotVisible(locator, timeout);
      if (toCheck) getElement.remove();
      if (locators.isEmpty()) return true;
      if (removeTimeoutAfterFirstCheck) {
        removeTimeoutAfterFirstCheck = false;
        timeout = 0.1;
      }
    }
    for (By fail : locators) {
      if (reportLog) {
        Log.addStepLog("Element Should Be Present=" + shouldBePresent
            + ", but opposite was found -> `" + fail.toString() + "` on " + currentUrl(), "error");
      } else {
        Log.warn("Element Should Be Present=" + shouldBePresent
            + ", but opposite was found -> `" + fail.toString() + "` on " + currentUrl());
      }
    }
    return false;
  }

  // Wait for locator to become "hidden" or not available on page
  public boolean waitForNotVisible(By locator, double timeout) {
    try {
      wait(timeout).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    } catch (Exception e) {
      Log.info("Element '" + locator.toString() + "' was still visible in '" + timeout
          + "' second(s) on page " + currentUrl());
      return false;
    }
    return true;
  }

  protected String getMyAttribute(By locator, String attribute) {
    return getMyAttribute(driver.findElement(locator), attribute);
  }

  // gets attribute value from locator. Handles null pointer exception.
  protected String getMyAttribute(WebElement locator, String attribute) {
    String attr = "";
    try {
      if (locator.getAttribute(attribute) != null) {
        attr = locator.getAttribute(attribute);
      }
    } catch (NullPointerException ignored) {}
    return attr;
  }

  // Gets and returns text from By locator
  public String getElementText(By locator) {
    return getElementText(driver.findElement(locator));
  }

  public String getElementText(WebElement element) {
    try {
      if (element.getText().isEmpty())
        return element.getAttribute("value");
      return element.getText();
    } catch (Exception e) {
      Log.warn("Exception: " + e + " getting text on : " + element.toString());
      return "";
    }
  }

  protected WebElement getRandomElement(By locator, int numStart) {
    if (!waitForElement(locator, 2)) return null;
    int random = getRandomNum(numStart, getElements(locator).size(), 10000);
    WebElement element = getElements(locator).get(random);
    Log.addStepLog("\nRandom element: " + locator + ": number " + random
        + ".\nText in element: " + getElementText(element));
    return element;
  }

  // Gets all the elements identified by "locator" and returns WebElement list.
  protected List<WebElement> getElements(By locator) {
    try {
      return driver.findElements(locator);
    } catch (WebDriverException e) {
      return null;
    }
  }

  public int getCount(By locator, int timeout) {
    if (timeout > 0)
      waitForElement(locator, timeout);
    return driver.findElements(locator).size();
  }

  public int getCount(By locator) {
    return getCount(locator, 2);
  }
}
