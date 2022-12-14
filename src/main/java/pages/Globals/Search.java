package pages.Globals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.tableUtils;

public class Search extends tableUtils {

  public Search(WebDriver driver) { super(driver); }

  // 1st locator: prices page; 2nd locator: funding page, 3rd locator: asset ticker.
  public final By searchFld = strToBy(
      ".search-input input, .search-bar input, [class*='searchInput']");

  public boolean enterTextInSearchField(String text) {
    return clearAndEnterText(searchFld, text);
  }

}
