package pages.Overview;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import main.Log;
import utils.tableUtils;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class OverviewPage extends tableUtils {

  public OverviewPage(WebDriver driver) {
    super(driver);
  }

  public final By overviewPageContent = strToBy("#overview");
  private final By overviewOnboarding = strToBy("[class*='module_onboardingBanner' i]");

  // Legacy locators
  private final By overviewPageLoadBar = strToBy(".account-wrap #trade-content-wrap .loading");

  public boolean overviewLandingPageLoaded() {
    Set<By> pageElements = new HashSet<>(Arrays.asList(overviewPageContent,
        overviewOnboarding));
    for (By el : pageElements) Log.addStepLog("Checking Overview element: " + byToStr(el));
    return waitForElements(pageElements, 6, true);
  }

  // If loading bar is not done loading and retry=true, will refresh page and run method once more
  public boolean overviewLoadingBarDone(int retry) {
    boolean initialLoad = waitForElement(overviewPageContent, 10);
    waitForElement(overviewPageLoadBar, 2); // May not show depending on load speed.
    boolean loadingBarGone = waitForNotVisible(overviewPageLoadBar, 10);
    if ((!initialLoad || !loadingBarGone) && retry > 0) {
      pageRefresh();
      return overviewLoadingBarDone(retry - 1);
    }
    return loadingBarGone;
  }
}
