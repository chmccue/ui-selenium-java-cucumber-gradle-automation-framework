package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class tableUtils extends clickUtils {

  public tableUtils(WebDriver driver) { super(driver); }

  protected final By tableHeader = By.cssSelector("thead");
  private final By tableBody = By.cssSelector("tbody");
  protected By tableRow = By.cssSelector("tbody tr");
  protected By tableLoadError = By.cssSelector("[data-lang-key='error:InternalError']");
  protected By tableLoadingIcon = addStrToBy(tableBody, " .loading-icon");
  protected final By tablePagination = strToBy(".table-pagination");

  public boolean clickOnTableRowLoc(By tableBaseElement, String textInRow, By toClick) {
    if (!tableRowsLoaded()) return false;
    WebElement row = getElementMatch(combineSelectors(tableBaseElement, tableRow), textInRow);
    if (row == null) return false;
    return clickOn(row.findElement(toClick));
  }

  public boolean clickOnTableRowText(By tableBaseElement, String textInRow, String textToClick) {
    if (!tableRowsLoaded()) return false;
    Map<String,String> matchers = new HashMap<>();
    matchers.put(textInRow, "text");
    matchers.put(textToClick, "text");
    WebElement row = getElementMatch(combineSelectors(
        tableBaseElement, tableRow), matchers, 1, true);
    // iterates through table element tags listed below in list
    for (String tag : new ArrayList<>(Arrays.asList("a", "button"))) {
      for (WebElement link : row.findElements(strToBy(tag))) {
        if (textHas(getElementText(link), textToClick)) {
          return clickOn(link);
        }
      }
    }
    return false;
  }

  // Can take an optional math symbol (>, =, <) separated by a space. Otherwise defaults to "=="
  public boolean tableRowValidateCount(By baseTable, String expected, String emptyTableStr) {
    if (!tableRowsLoaded()) return false;
    String[] expectedRowCountList = expected.split(" ");
    String mathSymbol = expectedRowCountList.length > 1 ? expectedRowCountList[0] : "==";
    int expRowCount = Integer.parseInt(expectedRowCountList[expectedRowCountList.length - 1]);
    int rowCount = getCount(combineSelectors(baseTable, tableRow));
    if (textHas(expected, "(< 1|^={0,2} ?0)")) {
      if (tableRowContains(baseTable, strToSet(emptyTableStr))) rowCount = 0;
    }
    return checkMathExpression(rowCount, mathSymbol, expRowCount);
  }

  protected boolean allTableRowsContain(By tableBaseElement, Map<String,String> matchers) {
    return tableRowContains(tableBaseElement, matchers, "all");
  }

  protected boolean noTableRowContains(By tableBaseElement, String matcher) {
    Map<String, String> newMatchers = new HashMap<>();
    newMatchers.put(matcher, "text");
    return getElementMatch(
        combineSelectors(tableBaseElement, tableRow), newMatchers, 0, true) == null;
  }

  protected boolean tableRowContains(By tableBaseElement, Set<String> matchers) {
    Map<String, String> newMatchers = new HashMap<>();
    for (String item : matchers) {
      newMatchers.put(item, "text");
    }
    return tableRowContains(tableBaseElement, newMatchers, 1);
  }

  // If row found matching matchers, returns true.
  private boolean tableRowContains(
      By tableBaseElement, Map<String,String> matchers, Object matchCount) {
    return tableRowsLoaded() && getElementMatch(
        combineSelectors(tableBaseElement, tableRow), matchers, matchCount, true) != null;
  }

  // If row found matching expectedText, returns true.
  public boolean tableHeaderContains(By headerBaseElement, List<String> expectedText) {
    return regexElementMatch(combineSelectors(headerBaseElement, tableHeader), expectedText);
  }

  // If row column found matching matcher in all rows, returns true.
  protected boolean tableRowColumnsContain(By tableBaseElement, By column, String matcher) {
    HashMap<String, String> matcherMap = new HashMap<>();
    matcherMap.put(matcher, "text");
    return tableRowsLoaded() && getElementMatch(combineSelectors(combineSelectors(
            tableBaseElement, tableRow), column), matcherMap, "all", true) != null;
  }

  private boolean tableRowsLoaded() {
    return waitForNotVisible(tableLoadingIcon, 10);
  }
}
