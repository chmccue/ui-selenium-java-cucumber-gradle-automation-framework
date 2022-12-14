package pages.Trade;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.clickUtils;
import java.text.DecimalFormat;

public class NewOrderPage extends clickUtils {

  public NewOrderPage(WebDriver driver) { super(driver); }

  public String realTimeOrderNumber;
  // Platform types
  private final By newOrdSimple = By.cssSelector("#order-simple");
  private final By newOrdInt = By.cssSelector("#order-int");
  private final By newOrdAdv = By.cssSelector("#order-adv");
  // Shared dynamic elements
  private final By newOrdBuyBtn = By.cssSelector("button[value='buy']");
  private final By newOrdSellBtn = By.cssSelector("button[value='sell']");
  private final By newOrdAmtField = By.cssSelector("[name='volume']");
  private final By newOrdPriceField = By.cssSelector("[name='price']");
  private final By newOrdTotalField = By.cssSelector("[name='total']");
  private final By newOrdVolToggle = By.cssSelector(".volume-currency-toggle");
  private final By newOrdOrderTypeMenu = By.cssSelector("[name='ordertype']");
  private final By newOrdLimitBtn = By.cssSelector("[value='limit']");
  private final By newOrdMarketBtn = By.cssSelector("[value='market']");
  private final By newOrdSubmitBtn = By.cssSelector("button.submit");
  private final By newOrdConfDetails = By.cssSelector(".review-wrap .well-data-list");
  private final By newOrdConfSubmitBtn = By.cssSelector(".review-wrap .btn-order-confirm");
  private final By newOrdConfBackBtn = By.cssSelector(".review-wrap .btn-order-back");
  private final By newOrdAmtHint = By.cssSelector(".control-group:nth-child(2) .control-hint");
  private final By newOrdVolSelectOption = By.cssSelector(".dropdown-menu [data-value]:not(.disabled)");
  private final By newOrdSuccessMsg = By.cssSelector(".order-complete[style*='display'] .alert-success");
  private final By newOrdFailMsg = By.cssSelector(".alert-error[style*='block']");
  private final By newOrdCreateNewBtn =
      By.cssSelector(".order-complete[style*='display'] .btn-order-new");
  // Shared static elements
  private final By newOrdVolToggleOpen = By.cssSelector(".dropdown.open .volume-currency-toggle");

  private final By newOrdOpenOrdersTable = By.cssSelector("#order-open-order tbody tr");
  private final By newOrdOpenOrdersCancel = By.cssSelector(".btn-cancel");

  public By setTradePlatformVar(String orderPlatform) {
    By platformType;
    if (textHas(orderPlatform,"int")) platformType = newOrdInt;
    else if (textHas(orderPlatform,"advance")) platformType = newOrdAdv;
    else platformType = newOrdSimple; // assumes simple form
    return platformType;
  }

  public boolean matchCopyOnSubmitBtn(String expectedText, String orderPlatform) {
    return regexElementMatch(
        combineSelectors(setTradePlatformVar(orderPlatform), newOrdSubmitBtn), expectedText);
  }

  // Use "exists" for Confirm Submit button because if fat finger warning appears, it is disabled
  public boolean onNewOrderConfirmDetailsPage(String orderPlatform) {
    By platformType = setTradePlatformVar(orderPlatform);
    hardWait(0.05);
    return waitForElement(combineSelectors(platformType, newOrdConfDetails), 2)
        && waitForElement(combineSelectors(platformType, newOrdConfBackBtn), 0.5)
        && exists(combineSelectors(platformType, newOrdConfSubmitBtn));
  }

  public boolean clickTradeNewOrderBtn(String selectItem, String orderPlatform) {
    By clickLink;
    boolean dropdown = false;
    By platformType = setTradePlatformVar(orderPlatform);
    switch (selectItem.toLowerCase()) {
      case "submit":
        return clickOn(combineSelectors(platformType, newOrdSubmitBtn));
      case "confirm":
        return onNewOrderConfirmDetailsPage(orderPlatform)
            && clickOn(combineSelectors(platformType, newOrdConfSubmitBtn));
      case "confirm back":
        return onNewOrderConfirmDetailsPage(orderPlatform)
            && clickOn(combineSelectors(platformType, newOrdConfBackBtn))
            && waitForNotVisible(combineSelectors(platformType, newOrdConfBackBtn), 3);
      case "buy":
        clickLink = newOrdBuyBtn;
        break;
      case "sell":
        clickLink = newOrdSellBtn;
        break;
      case "limit":
        clickLink = newOrdLimitBtn;
        if (orderPlatform.equalsIgnoreCase("advanced")) {
          dropdown = true;
        }
        break;
      case "market":
        clickLink = newOrdMarketBtn;
        if (orderPlatform.equalsIgnoreCase("advanced")) {
          dropdown = true;
        }
        break;
      case "create new order":
        return clickOn(combineSelectors(platformType, newOrdCreateNewBtn))
            && waitForNotVisible(combineSelectors(platformType, newOrdCreateNewBtn), 3);
      default:
        Log.info("Item " + selectItem + " not implemented on trade page");
        return false;
    }
    clickLink = combineSelectors(platformType, clickLink);
    By linkActive = addStrToBy(clickLink, ".active");
    if (dropdown) {
      return selectDropdownOption(combineSelectors(platformType, newOrdOrderTypeMenu), clickLink);
    }
    return clickOnAndWait(clickLink, linkActive, 4);
  }

  // select type of currency
  public boolean toggleCurrencyAmountType(String orderPlatform) {
    By volMenu = combineSelectors(setTradePlatformVar(orderPlatform), newOrdVolToggle);
    By volOption = combineSelectors(setTradePlatformVar(orderPlatform), newOrdVolSelectOption);
    clickOnAndWait(volMenu, newOrdVolToggleOpen, 1);
    String getDropdownText = getElementText(volOption);
    return clickOn(volOption) && regexMatch(getDropdownText, getElementText(volMenu));
  }

  // enter the amount to buy/sell and check price
  public boolean enterAmountAndVerifyTotal(
      String purchaseAmt, String purchasePrice, String orderPlatform) {
    By platformType = setTradePlatformVar(orderPlatform);
    By enterAmt = combineSelectors(platformType, newOrdAmtField);
    By enterAmtHint = combineSelectors(platformType, newOrdAmtHint);
    By enterPrice = combineSelectors(platformType, newOrdPriceField);
    By totalField = combineSelectors(platformType, newOrdTotalField);
    double calculateTotal;
    // if buy/sell appears in enterAmtHint, calculates via multiplication. Otherwise division.
    if (elementHas(enterAmtHint, "to (buy|sell)")) {
      calculateTotal = Double.parseDouble(purchaseAmt) * Double.parseDouble(purchasePrice);
    } else {
      calculateTotal = Double.parseDouble(purchaseAmt) / Double.parseDouble(purchasePrice);
    }
    clearAndEnterText(enterAmt, purchaseAmt);
    clearAndEnterText(enterPrice, purchasePrice);
    String calculateRange = Math.floor(calculateTotal) + "-" + (Math.floor(calculateTotal) + 1);
    return regexMatch(calculateRange,
        Double.toString(Math.floor(Double.parseDouble((getElementText(totalField))))));
  }

  public boolean orderConfirmationMsg(String passOrFail, String orderPlatform) {
    By platformType = setTradePlatformVar(orderPlatform);
    if (passOrFail.equalsIgnoreCase("success")) {
      return waitForElement(combineSelectors(platformType, newOrdSuccessMsg), 2);
    } else {
      return waitForElement(combineSelectors(platformType, newOrdFailMsg), 2);
    }
  }

  public boolean cancelOpenOrder(String orderNumber) {
    return cancelOpenOrder(orderNumber, 1);
  }

  private boolean cancelOpenOrder(String orderNumber, int retryCount) {
    By orderCancelBtn = newOrdOpenOrdersCancel;
    hardWait(0.2);
    if (orderNumber.equalsIgnoreCase("all")) {
      int startingOrderCount = getCount(combineSelectors(newOrdOpenOrdersTable, orderCancelBtn));
      for (int i = startingOrderCount; i > 0; i--) {
        orderCancelBtn = combineSelectors(addStrToBy(
            newOrdOpenOrdersTable, ":nth-child(" + i + ") "), newOrdOpenOrdersCancel);
        if (!clickOn(orderCancelBtn) || !waitForNotVisible(orderCancelBtn, 8)) {
          if (retryCount > 0) {
            retryCount -= 1;
            return cancelOpenOrder(orderNumber, retryCount);
          } else {
            return false;
          }
        }
      }
    } else {
      By orderRow = By.cssSelector("#" + orderNumber);
      if (!waitForElement(orderRow, 10)) return false;
      orderCancelBtn = combineSelectors(orderRow, orderCancelBtn);
      return regexMatch(orderNumber, getMyAttribute(orderRow, "id"))
          && clickOn(orderCancelBtn, 5) && waitForNotVisible(orderCancelBtn, 8);
    }
    return true;
  }

  public boolean newOrderFoundInNewOrderList(String platformType) {
    By successMsg = combineSelectors(setTradePlatformVar(platformType), newOrdSuccessMsg);
    realTimeOrderNumber = regexParse(getElementText(successMsg), "([A-Z0-9]{5,6}(-|)){3}");
    Log.addStepLog("Order Number: " + realTimeOrderNumber);
    int retry  = 10;
    while (retry > 0) {
      boolean compareOrder =
          regexMatch(realTimeOrderNumber, getMyAttribute(newOrdOpenOrdersTable, "id"));
      if (compareOrder) return true;
      retry -= 1;
      hardWait(1);
    }
    return false;
  }

  // Have to use this instead of global alert method because it builds with local variables.
  public boolean checkNewOrderFailMessage(String platformType, String failMsg) {
    By buildNewOrdFailMsg = combineSelectors(setTradePlatformVar(platformType), newOrdFailMsg);
    waitForElement(buildNewOrdFailMsg, 3);
    return regexElementMatch(buildNewOrdFailMsg, failMsg);
  }

  public boolean newOrderEnterBelowMin(
      String tradeType, String orderForm, String belowMinVolume, String purchasePrice) {
    clickTradeNewOrderBtn(tradeType, orderForm);
    int runAttempts = 4;
    while (runAttempts > 0) {
      if (enterAmountAndVerifyTotal(belowMinVolume, purchasePrice, orderForm)
          && clickTradeNewOrderBtn("Submit", orderForm)
          && checkNewOrderFailMessage(orderForm, "Amount too low")) {
        return true;
      }
      hardWait(0.5);
      runAttempts -= 1;
    }
    return false;
  }

  public String calculateBelowMinVolume(String minVolume) {
    double belowMin = Double.parseDouble(minVolume);
    belowMin = (belowMin > 0.001) ? belowMin - 0.001 : belowMin - 0.000001;
    return new DecimalFormat("##.########").format(belowMin);
  }

  public boolean newOrderPressSubmitToConfirmPage(String buyOrSell, String orderForm, int retry) {
    clickTradeNewOrderBtn(buyOrSell, orderForm);
    boolean clickSubmitAndOnConfirmDetails = clickTradeNewOrderBtn("Submit", orderForm)
        && onNewOrderConfirmDetailsPage(orderForm);
    if (!clickSubmitAndOnConfirmDetails && retry > 0) {
      return newOrderPressSubmitToConfirmPage(buyOrSell, orderForm, retry - 1);
    }
    return clickSubmitAndOnConfirmDetails;
  }

  public boolean newOrderEnterExpectedMin(
      String buyOrSell, String orderForm, String minVolume, String purchasePrice) {
    int runAttempts = 5;
    boolean enterAndSubmit = enterAmountAndVerifyTotal(minVolume, purchasePrice, orderForm)
        && newOrderPressSubmitToConfirmPage(buyOrSell, orderForm, runAttempts);
    while (!clickTradeNewOrderBtn("confirm back", orderForm) && runAttempts > 0) {
      hardWait(0.5);
      runAttempts =- 1;
    }
    return enterAndSubmit;
  }
}
