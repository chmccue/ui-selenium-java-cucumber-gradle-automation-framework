package steps;

import static main.Core.skipStep;
import static utils.matcherUtils.textHas;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.After;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.GlobalNav.AssetTicker;
import pages.GlobalNav.AssetPairs;
import pages.Trade.NewOrderPage;
import pages.Trade.OrdersPage;
import java.util.List;
import java.util.Arrays;

public class TradePageSteps implements Base {

  private final OrdersPage ordersPage = new OrdersPage(driver);
  private final NewOrderPage newOrderPage = new NewOrderPage(driver);
  private final AssetTicker assetTicker = new AssetTicker(driver);
  private final AssetPairs assetPairs = new AssetPairs();

  @After(value = "@teardown-cancel-all-open-orders", order = 1)
  public void teardownCancelAllOpenOrders() {
    Log.info("teardown: cancel all open orders");
    if (!navMenu.clickLoggedInPrimaryNav("trade")) {
      page.pageRefresh();
      navMenu.clickLoggedInPrimaryNav("trade");
    }
    navMenu.clickNavLink("sub account", "new-order");
    navMenu.clickNavLink("order form", "simple");
    newOrderPage.cancelOpenOrder("all");
  }

  @Then("^I click \"([^\"]*)\" (button|item) on \"([^\"]*)\" order$")
  public void clickTrade(String selectItem, String item, String orderPlatform) {
    Log.info("I click " + selectItem + " " + item + " on " + orderPlatform + " order");
    Assert.assertTrue(newOrderPage.clickTradeNewOrderBtn(selectItem, orderPlatform));
  }

  @Then("^I switch volume dropdown option for \"([^\"]*)\" order$")
  public void selectCurrencyAmountType(String orderPlatform) {
    Log.info("I switch volume dropdown option for " + orderPlatform + " order");
    Assert.assertTrue(newOrderPage.toggleCurrencyAmountType(orderPlatform));
  }

  @Then("^for limit (?:|I )enter \"([^\"]*)\" amount and \"([^\"]*)\" price on \"([^\"]*)\" order$")
  public void enterAmount(String purchaseAmt, String purchasePrice, String orderPlatform) {
    Log.info("for limit I enter " + purchaseAmt + " amount and " + purchasePrice
            + " price on " + orderPlatform + " order");
    Assert.assertTrue(newOrderPage.enterAmountAndVerifyTotal(purchaseAmt,
            purchasePrice, orderPlatform));
  }

  @Then("^I complete \"([^\"]*)\" (Buy|Sell) order expecting \"(success|fail)\"$")
  public void completeTransaction(String orderPlatform, String buyOrSell, String result) {
    Log.info("I complete " + orderPlatform + " " + buyOrSell + " order expecting " + result);
    Assert.assertTrue(newOrderPage.matchCopyOnSubmitBtn(buyOrSell, orderPlatform));
    Assert.assertTrue(newOrderPage.newOrderPressSubmitToConfirmPage(buyOrSell, orderPlatform, 2));
    Assert.assertTrue(newOrderPage.clickTradeNewOrderBtn("confirm", orderPlatform));
    Assert.assertTrue(newOrderPage.orderConfirmationMsg(result, orderPlatform));
    if (result.equalsIgnoreCase("success")) {
      Assert.assertTrue(newOrderPage.newOrderFoundInNewOrderList(orderPlatform));
    }
  }

  @Then("^I verify Orders table is on the Orders Trade page$")
  public void getOrdersTableValue() {
    if (skipStep) {
      Log.logSkipStepMsg();
      return;
    }
    Log.info("I verify Orders table is on the Orders Trade page");
    Assert.assertTrue(ordersPage.checkOrderListTableValue());
  }

  @Then("^I cancel last open order on New Order page$")
  public void cancelOpenOrderFromNewOrderPage() {
    Log.info("I cancel last open order on New Order page");
    Assert.assertTrue(newOrderPage.cancelOpenOrder(newOrderPage.realTimeOrderNumber));
  }

  @Then("^I confirm \"([^\"]*)\" fail message on \"([^\"]*)\" New Order page$")
  public void newOrderFailMessage(String failMessage, String platformType) {
    Log.info(
        "I confirm '" + failMessage + "' fail message on " + platformType + " New Order page");
    Assert.assertTrue(newOrderPage.checkNewOrderFailMessage(platformType, failMessage));
  }

  // List values must be entered in following order:
  // | asset | at min volume |
  @Then("^test new order submit for below min and at min volume sizes:$")
  public void checkNewOrderSubmitMinimums(DataTable data) {
    Log.info("test new order submit for below min and at min volume sizes:\n" + data);
    List<String> params = page.dtToList(data);
    String enteredAsset = params.get(0);
    String atMinVolume = params.get(1);
    String belowMinVolume = newOrderPage.calculateBelowMinVolume(params.get(1));
    for (String orderForm : Arrays.asList("Simple", "Intermediate", "Advanced")) {
      new NavigationSteps().clickItemInNav(orderForm, "order form");
      for (String assetPair : assetPairs.getAssetList(enteredAsset)) {
        assetPair = assetTicker.setAssetPath(enteredAsset, assetPair);
        String purchasePrice = textHas(assetPair, "(jpy|yfi)") ? "1" : "0.1";
        Log.addStepLog(orderForm + " Buy/Sell: " + assetPair);
        if (!assetTicker.assetTickerCurrencyMenu(assetPair)) {
          steps.fails.add("Selecting asset ticker: " + assetPair);
        }
        for (String buyOrSell : Arrays.asList("buy", "sell")) {
          if (!newOrderPage.newOrderEnterBelowMin(
              buyOrSell, orderForm, belowMinVolume, purchasePrice)) {
            steps.fails.add("Below minimum: " + orderForm + " " + buyOrSell + " -> " + assetPair);
          }
          if (!newOrderPage.newOrderEnterExpectedMin(
              buyOrSell, orderForm, atMinVolume, purchasePrice)) {
            steps.fails.add("At minimum: " + orderForm + " " + buyOrSell + " -> " + assetPair);
            if (newOrderPage.onNewOrderConfirmDetailsPage(orderForm)) {
              clickTrade("confirm back", "button", orderForm);
            }
          }
        }
        if (steps.fails.size() > 5) {
          Log.addStepLog("More than 5 fails reported. Stopping the test, as this may be a "
              + "broader site problem. See error output for additional details.", "error");
          steps.failHandler();
        }
      }
    }
    steps.failHandler();
  }
}
