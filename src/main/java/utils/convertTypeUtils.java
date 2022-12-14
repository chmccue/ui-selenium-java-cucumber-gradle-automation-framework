package utils;

import static utils.matcherUtils.textHas;

import io.cucumber.datatable.DataTable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class convertTypeUtils extends helperUtils {

  convertTypeUtils(WebDriver driver) { super(driver); }

  // Takes a string separated by commas and outputs a List<String> separated by each comma.
  public List<String> strToLst(String toConvert) {
    return new ArrayList<>(Arrays.asList(toConvert.split(",( )?")));
  }

  // Takes a string separated by commas and outputs a HashSet<String> separated by each comma.
  protected Set<String> strToSet(String toConvert) {
    return new HashSet<>(Arrays.asList(toConvert.split(",( )?")));
  }

  // Returns String locator portion of submitted css/xpath By element.
  // Example: byToStr(By.cssSelector(".mainString")) -> ".mainString"
  public String byToStr(By locator) {
    return locator.toString().replaceFirst("By.(cssSelector|xpath):( )?", "");
  }

  public By strToBy(String value) {
    if (textHas(value, "//")) return By.xpath(value);
    return By.cssSelector(value);
  }

  // Pass in a Datatable to convert into List of Strings with no null/empty values
  public List<String> dtToList(DataTable dt) {
    List<String> finalList = new ArrayList<>();
    List<List<String>> rows = dt.asLists(String.class);
    for (List<String> row : rows) {
      for (String col : row) {
        if (col == null || col.isEmpty()) continue;
        finalList.add(col);
      }
    }
    return finalList;
  }
}
