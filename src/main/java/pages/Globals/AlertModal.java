package pages.Globals;

import static main.Core.firefoxTest;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;

public class AlertModal extends ToolTips {

  public AlertModal(WebDriver driver) { super(driver); }

  // Alert message types
  private static final By globalAlertSuccess = By.cssSelector(
      "[class*='AlertBar-module_success' i], .alert-success, .alert-bar[class*='success']");
  // first 2 locators below are sb errors. Others may no longer be needed once sb is implemented.
  public static final By globalAlertFail = By.cssSelector("[data-testid*='error' i], "
      + "[class*='Notice-module_error'], [class*='alert'] [class*='error']:not(.hidden), "
      + ".alert-error, .panel.error");
  private final By globalAlertDanger   = strToBy(".alert-danger");
  private final By globalAlertInfo     = strToBy(".alert:not(.alert-success):not(.alert-error)");
  private final By globalAlertWarning  = strToBy("[class*='notice-module_warning' i]");
  private final By globalAlert         = strToBy("[class*='notice-module' i], .alert, .alert-bar");
  private final By globalAlertCloseBtn = strToBy("[aria-hidden='false'] "
      + "[data-testid*='close alert'], .alert-container :not(.hidden) .dismiss");
  private final By acceptCookieBtn     = strToBy(".cookie-warning .accept");

  // The list arg can take up to 2 comma separated strings, or 1 value (no comma). If only 1 value,
  // will validate alert type and check alert text is not empty. If 2 values, will validate alert
  // type and full or partial alert message
  private boolean checkAlertTypeAndMessage(List<String> alertData, int retry) {
    By alert;
    String alertType = alertData.get(0);
    if (textHas(alertType, "success")) alert = globalAlertSuccess;
    else if (textHas(alertType, "info")) alert = globalAlertInfo;
    else if (textHas(alertType, "warning")) alert = globalAlertWarning;
    else if (textHas(alertType, "fail")) alert = globalAlertFail;
    else if (textHas(alertType, "danger")) alert = globalAlertDanger;
    else alert = globalAlert;

    hardWait(0.2);
    try {
      boolean alertFound = waitForElement(alert, 3) && getElementText(alert).length() > 0;
      if (alertData.size() == 1) return alertFound;
      String alertMsg = alertData.get(alertData.size() - 1);
      if (alertFound) alertFound = regexElementMatch(alert, alertMsg);
      return !alertFound && retry > 0 ? checkAlertTypeAndMessage(alertData, retry - 1) : alertFound;
    } catch (NullPointerException e) {
      return checkAlertTypeAndMessage(alertData, retry - 1);
    }
  }

  public boolean checkAlertTypeAndMessage(List<String> alertData) {
    return checkAlertTypeAndMessage(alertData, 3);
  }

  // for alerts that display a close btn.
  public boolean closeAlertMessage() {
    if (firefoxTest) hardWait(0.2);
    return closeAlertMessage(true);
  }

  private boolean closeAlertMessage(boolean retry) {
    boolean closeAlert = true;
    if (waitForElement(globalAlertCloseBtn, 1, false)) {
      closeAlert = clickOn(globalAlertCloseBtn)
          && waitForNotVisible(globalAlertCloseBtn, 1);
      if (!closeAlert && retry) return closeAlertMessage(false);
    }
    hardWait(0.2);
    return closeAlert;
  }

  public boolean acceptCookieAlertIfOpen() {
    if (exists(acceptCookieBtn)) {
      return clickOn(acceptCookieBtn) && waitForNotVisible(acceptCookieBtn, 2);
    }
    return true;
  }
}
