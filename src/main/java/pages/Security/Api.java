package pages.Security;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.GlobalModal;
import pages.Globals.Loading;

public class Api extends GlobalModal {

  public Api(WebDriver driver) { super(driver); }

  public final By apiAddKeyTableBtn = strToBy("[data-testid='api key new']");
  private final By apiKeyTableDeleteBtn = strToBy("[data-testid='removeAPIKey']");
  private final By apiKeyNameFld = strToBy("[name='apiKeyName']");
  public final By apiKeyTable = strToBy("[data-testid='api keys table']");
  private final By apiKeyEmptyTable = strToBy("[data-testid='no api keys']");
  private final By apiNonceWindowFld = strToBy("[name='nonceWindow']");
  private final By apiKeyExpDateSwitch = strToBy("[data-testid='keyExpiration' i] [type*='box']");
  private final By apiQueryStartDateSwitch = strToBy(
      "[data-testid*='queryStart' i] [type*='box']");
  private final By apiQueryEndDateSwitch = strToBy("[data-testid*='queryEnd' i] [type*='box']");
  private final By apiKeyExpDateFld = strToBy("[name='keyExpirationDate' i]");
  private final By apiQueryStartDateFld = strToBy("[name='queryStartDate' i]");
  private final By apiQueryEndDateFld = strToBy("[name='queryEndDate' i]");

  private final By apiCalendarMonth = strToBy(".DayPicker");
  private final By apiCalendarPrevMonth = strToBy("[aria-label*='previous' i]");
  private final By apiCalendarNextMonth = strToBy("[aria-label*='next' i]");
  private final By apiCalendarDay = strToBy(".DayPicker-Day:not([aria-disabled='true'])");
  private final By apiCalendarTime = strToBy(".day-picker-controls input");

  public boolean toggleApiSwitch(String buttonName, String onOrOff) {
    By waitFor = setCalendarFld(buttonName);
    By toClick = apiKeyExpDateSwitch;
    if (textHas(buttonName, "Query start")) toClick = apiQueryStartDateSwitch;
    else if (textHas(buttonName, "Query end")) toClick = apiQueryEndDateSwitch;
    return textHas(onOrOff, "on")
        ? clickOnAndWait(toClick, waitFor, 2) : clickOn(toClick) && waitForNotVisible(waitFor, 2);
  }

  public boolean apiSwitchOpenCalendar(String buttonName) {
    return clickOnAndWait(setCalendarFld(buttonName), apiCalendarMonth, 1);
  }

  public boolean apiCalendarFieldNotEmpty(String buttonName) {
    return !getElementText(setCalendarFld(buttonName)).isEmpty();
  }

  public boolean selectCalendarDateTime() {
    boolean selectDay;
    try {
      selectDay = waitForElement(apiCalendarPrevMonth, 1) && clickOn(apiCalendarNextMonth)
          && clickOnRandom(apiCalendarDay, 1);
    } catch (IndexOutOfBoundsException e) {
      selectDay = clickOn(apiCalendarNextMonth) && clickOnRandom(apiCalendarDay, 1);
    }
    boolean selectTime = clearAndEnterText(apiCalendarTime, "123456");
    pressEscBtn();
    return selectDay && selectTime && waitForNotVisible(apiCalendarMonth, 2);
  }

  private By setCalendarFld(String fieldName) {
    By field = apiKeyExpDateFld;
    if (textHas(fieldName, "Query start")) field = apiQueryStartDateFld;
    else if (textHas(fieldName, "Query end")) field = apiQueryEndDateFld;
    return field;
  }

  // Enters the given value in the input field specified by textField
  // clears text before entering if clear is true
  public boolean enterApiFieldText(String textField, String value) {
    By input = textHas(textField, "Nonce Window") ? apiNonceWindowFld : apiKeyNameFld;
    By clickAway = textHas(textField, "Nonce Window") ? apiKeyNameFld : apiNonceWindowFld;
    return clearAndEnterText(input, value) && clickOn(clickAway)
        && getMyAttribute(input, "value").equalsIgnoreCase(value);
  }

  public boolean editApiKey(String keyName) {
    return waitForElement(apiKeyTable, 10) && clickOnTableRowText(apiKeyTable, keyName, "^edit$")
        && waitForElement(apiKeyNameFld, 3);
  }

  // to delete the top most api key regardless of the key's name, enter keyName="" (empty string)
  public boolean deleteApiKey(String keyName) {
    return waitForElement(apiKeyTable, 10)
        && (textHas(keyName, "^$") ? clickOn(apiKeyTableDeleteBtn)
        : clickOnTableRowText(apiKeyTable, keyName, "remove"))
        && globalClickModalBtn("confirm") && new Loading(driver).waitForLoadSpinnerToDisappear()
        && (!exists(apiKeyTable) || noTableRowContains(apiKeyTable, keyName));
  }

  public boolean deleteAllApiKeys() {
    while (exists(apiKeyTable)) {
      boolean deleteKey = clickOnTableRowText(apiKeyTable, "remove", "remove")
          && globalClickModalBtn("confirm");
      if (!deleteKey) return false;
      hardWait(0.1);
    }
    return waitForElement(apiKeyEmptyTable, 2);
  }
}
