package pages.Globals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.elementUtils;

public class Loading extends elementUtils {

  public Loading(WebDriver driver) { super(driver); }

  private final By globalLoadingSpinner = By.cssSelector("[class*='loadingspinner' i]");

  public boolean waitForLoadSpinnerToDisappear(boolean retry) {
    hardWait(0.15);
    boolean loadingDone = waitForNotVisible(globalLoadingSpinner, 10);
    if (!loadingDone && retry) return waitForLoadSpinnerToDisappear(false);
    return loadingDone;
  }

  public boolean waitForLoadSpinnerToDisappear() {
    return waitForLoadSpinnerToDisappear(false);
  }
}
