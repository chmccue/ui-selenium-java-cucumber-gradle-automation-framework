package utils;

import static main.Core.firefoxTest;

import main.Log;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class inputUtils extends elementUtils {

  inputUtils(WebDriver driver) { super(driver); }

  public void hover(By locator) {
    waitForPageReload(5);
    Actions action = new Actions(driver);
    waitForElement(locator, 2);
    scrollScreen(locator);
    hardWait(0.1);
    action.moveToElement(driver.findElement(locator)).build().perform();
    hardWait(0.3);
  }

  protected boolean hoverAndWait(By locatorToHover, By locatorToWait) {
    return hoverAndWait(locatorToHover, locatorToWait, 3);
  }

  private boolean hoverAndWait(By locatorToHover, By locatorToWait, int retry) {
    hover(locatorToHover);
    boolean locatorDisplayed = waitForElement(locatorToWait, 1);
    if (!locatorDisplayed && retry > 0) {
      retry -= 1;
      if (firefoxTest) hoverOffElement();
      return hoverAndWait(locatorToHover, locatorToWait, retry);
    }
    return locatorDisplayed;
  }

  protected void hoverOffElement() {
    ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,10000);");
    hardWait(0.5);
    hover(By.cssSelector("body"));
  }

  public boolean enterText(By element, String text) {
    if (!driver.findElement(element).isDisplayed())
      scrollScreen(element);
    return enterText(driver.findElement(element), text);
  }

  public boolean enterText(WebElement element, String text) {
    try {
      if (text.isEmpty()) {
        element.sendKeys(" ");
        hardWait(0.05);
        element.sendKeys(Keys.BACK_SPACE);
      } else if (firefoxTest) {
        for (char ch : text.toCharArray()) {
          element.sendKeys(String.valueOf(ch));
        }
      } else {
        element.sendKeys(text);
      }
    } catch (Exception e) {
      Log.error("Exception entering text " + text + " in web element: " + e);
      return false;
    }
    return true;
  }

  public void clearText(By locator) {
    waitForElement(locator, 3);
    try {
      driver.findElement(locator).clear();
    } catch(Exception ignored) {}
    while (getMyAttribute(locator, "value").length() > 0) {
      driver.findElement(locator).sendKeys(Keys.BACK_SPACE);
    }
  }

  public boolean clearAndEnterText(By locator, String text) {
    clearText(locator);
    return enterText(locator, text);
  }

  public void pressEscBtn() {
    new Actions(driver).sendKeys(Keys.ESCAPE).build().perform();
    driver.findElement(strToBy("body")).sendKeys(Keys.ESCAPE);
  }
}
