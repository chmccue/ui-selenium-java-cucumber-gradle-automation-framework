package pages.Trade;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.tableUtils;

public class OrdersPage extends tableUtils {

  public OrdersPage(WebDriver driver) { super(driver); }

  private final By orderListTable = By.cssSelector("#orderlist-wrap");

  // get values from the Rates, NewOrders, Ledger and Balance Table
  public boolean checkOrderListTableValue() {
    By rowValue = combineSelectors(orderListTable, tableRow);
    return waitForElement(rowValue, 2) && getCount(rowValue) > 0;
  }
}
