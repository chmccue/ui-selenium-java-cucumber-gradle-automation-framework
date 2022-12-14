package utils;

import main.Driver;
import main.Log;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class helperUtils extends Driver {

  helperUtils(WebDriver driver) { super(driver); }

  WebDriverWait wait(double waitTime) {
    return new WebDriverWait(driver, Duration.ofSeconds((long) waitTime));
  }

  public void pageRefresh() {
    try {
      driver.navigate().refresh();
      waitForPageReload(5);
    } catch (WebDriverException ignored) {}
  }

  public void pageBack() {
    try {
      driver.navigate().back();
      waitForPageReload(5);
    } catch (TimeoutException e) {
      pageRefresh();
    }
  }

  public String currentUrl() {
    try {
      return driver.getCurrentUrl().replaceAll("://.*?@", "://");
    } catch (Exception e) {
      Log.warn("Exception encountered during get currentUrl: " + e.toString());
      return e.toString();
    }
  }

  public String currentTitle() {
    try {
      return driver.getTitle();
    } catch (Exception e) {
      Log.warn("Exception encountered during get currentTitle: " + e.toString());
      return e.toString();
    }
  }

  public boolean waitForPageReload(double timeout) {
    try {
      timeout = Math.max(timeout, Driver.maxWaitSeconds);
      wait(timeout).until(webDriver -> ((JavascriptExecutor) webDriver)
          .executeScript("return document.readyState").equals("complete"));
    } catch (Exception e) {
      Log.warn("Error waiting for page to reload: " + e);
      return false;
    }
    return true;
  }

  public void scrollScreenToBottom() {
    Long pageSize = (Long) ((JavascriptExecutor) driver).executeScript(
        "return document.documentElement.scrollHeight");
    // if pageSize is equal to or smaller than current browser height, we exit.
    if (driver.manage().window().getSize().getHeight() > pageSize) return;
    long scrollTo = pageSize / 2;
    while (scrollTo < pageSize + 1) {
      ((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + scrollTo + ")");
      scrollTo = scrollTo * 2;
      hardWait(0.07);
    }
  }

  protected String randomStrGenerator(int len) {
    String data = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder randomStr = new StringBuilder(len);
    for (int i = 0; i < len; i++)
      randomStr.append(data.charAt(new SecureRandom().nextInt(data.length())));
    return randomStr.toString();
  }

  // Uses seconds arg, not milliseconds: 0.1 s = 100 ms, 1 s = 1000 ms, etc.
  public void hardWait(double seconds) {
    try { Thread.sleep(Math.round(seconds * 1000)); } catch (Exception ignored) {}
  }

  protected int getRandomNum(int rangeEnd) {
    return getRandomNum(1, rangeEnd, 10000);
  }

  protected int getRandomNum(int rangeStart, int rangeEnd, int max) {
    if (rangeEnd > max) rangeEnd = max;
    if (rangeStart == rangeEnd) return rangeStart;
    return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
  }

  // Example: checkMathExpression(5, ">", 3) -> returns true;
  boolean checkMathExpression(int numberOne, String mathSymbol, int numberTwo) {
    boolean checkSearchCount;
    if (">".equals(mathSymbol)) {
      checkSearchCount = numberOne > numberTwo;
    } else if (">=".equals(mathSymbol)) {
      checkSearchCount = numberOne >= numberTwo;
    } else if ("<=".equals(mathSymbol)) {
      checkSearchCount = numberOne <= numberTwo;
    } else if ("<".equals(mathSymbol)) {
      checkSearchCount = numberOne < numberTwo;
    } else {
      checkSearchCount = numberOne == numberTwo;
    }
    return checkSearchCount;
  }
}
