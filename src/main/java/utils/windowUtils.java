package utils;

import static main.Core.siteUrl;
import static utils.matcherUtils.textHas;

import main.Log;
import main.Core;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class windowUtils extends convertTypeUtils {

  windowUtils(WebDriver driver) { super(driver); }

  public String currentWindowId() {
    return currentWindowId(1);
  }

  private String currentWindowId(int retry) {
    try {
      waitForPageReload(5);
      return driver.getWindowHandle();
    } catch (WebDriverException e) {
      if (retry > 0) {
        pageRefresh();
        hardWait(3);
        return currentWindowId(retry - 1);
      }
      return driver.getWindowHandle();
    }
  }

  // windowIdentifier can be page title, page url, or window handle ID
  boolean switchWindowControl(String windowIdentifier, int retryCount) {
    String startingWindow = currentWindowId(); // Save WindowHandle of Parent Browser Window
    try {
      Set<String> allWindows = driver.getWindowHandles();
      for (String windowId : allWindows) {
        driver.switchTo().window(windowId);
        waitForPageReload(2);
        windowIdentifier = urlSupportBotHandler(windowIdentifier);
        if (Core.remoteTest) hardWait(0.1);
        // Below workaround for dev pipeline linkedin. It works correctly in prod pipeline.
        if (!textHas(Core.siteUrl, Core.prodEnvs + "\\.company")
            && Core.remoteTest && textHas(currentUrl(), "linkedin")) {
          return true;
        }
        if (!textHas(currentUrl(), "/redirect") && (textHas(currentTitle(), windowIdentifier)
            || textHas(currentUrl(), windowIdentifier)
            || currentWindowId().equals(windowIdentifier))) {
          // alpha-trade url goes to page with no content, so we just validate we got to the page.
          if (textHas(Core.siteUrl, Core.prodEnvs + "\\.company")
              && textHas(currentUrl(), Core.prodEnvs + "-trade\\.company.com")) {
            return true;
          }
          return pageLoadsWithoutKnownErrors();
        }
      }
    } catch (Exception e) {
      Log.warn("Error found when switching between windows:\n" + e.toString());
    }
    if (retryCount > 0) {
      Log.warn("Did not find window match for " + windowIdentifier + "\nCurrent title: "
          + currentTitle() + "\nCurrent url: " + currentUrl() + "\nRetrying...");
      hardWait(2); // minor delay to give test short break before retrying.
      return switchWindowControl(windowIdentifier, retryCount - 1);
    }
    Log.addStepLog(
        "'" + windowIdentifier + "' not found in any open browser windows/tabs.", "error");
    driver.switchTo().window(startingWindow);
    return false;
  }

  public boolean pageLoadsWithoutKnownErrors() {
    waitForPageReload(10);
    List<String> badPageErrors = Arrays.asList(
        "503 Service Temporarily Unavailable",
        "Page Not Found",
        "HTTP ERROR \\d\\d\\d",
        "502 Bad Gateway",
        "Oops! That page can’t be found");
    String pageText = driver.findElement(strToBy("html")).getText();
    for (String error : badPageErrors) {
      if (textHas(pageText, error)) {
        Log.addStepLog("Found text: " + error, "error");
        return false;
      }
    }
    if (textHas(currentTitle(), "Company - (500|404)") || textHas(currentUrl(), "/(500|404)/?$")) {
      Log.addStepLog("Found error in page title and/or url:<br />\nTitle: " + currentTitle()
          + "<br />\nUrl: " + currentUrl(), "error");
      return false;
    }
    String nonEmptyBody = "(?s)body.*?>.+<\\/body>";
    if (!textHas(driver.getPageSource(), nonEmptyBody)) {
      pageRefresh();
      if (!textHas(driver.getPageSource(), nonEmptyBody)) {
        Log.addStepLog("Page encountered no html body, even after page refresh", "error");
        return false;
      }
    }
    return true;
  }

  private String urlSupportBotHandler(String urlOrTitle) {
    if (Core.chromeTest && Core.remoteTest) {
      if (textHas(currentUrl(), "support\\.company")
          && textHas(currentTitle(), "Security Check")) {
        Log.addStepLog("Encountered Security Check Support page when expecting validator "
            + urlOrTitle);
        urlOrTitle = "support\\.company";
      }
    }
    return urlOrTitle;
  }

  public String combineSiteUrlAndRelativeUrl(String relativeUrl) {
    // if we enter a url that starts with http, we simply return that.
    if (textHas(relativeUrl, "^http")) return relativeUrl;
    // we ensure there is not double "//" connecting siteUrl and relativeUrl
    if (textHas(siteUrl, "/$") && textHas(relativeUrl, "^/")) {
      relativeUrl = relativeUrl.replaceFirst("/", "");
    }
    return siteUrl + relativeUrl;
  }
}
