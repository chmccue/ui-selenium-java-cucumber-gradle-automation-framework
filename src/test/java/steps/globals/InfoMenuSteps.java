package steps.globals;

import static utils.matcherUtils.textHas;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import main.Base;
import main.Log;
import org.junit.Assert;
import pages.Globals.InfoMenu;

public class InfoMenuSteps implements Base {

  private final InfoMenu menu = new InfoMenu(driver);

  @Then("^I verify there are \"([^\"]*)\" (accordion|circle) items$")
  public void accordionCircleCompareItemCount(int expectedCount, String itemType) {
    Log.info("I verify there are " + expectedCount + " " + itemType + " items");
    if (textHas(itemType, "accordion")) {
      if (expectedCount == 0) Assert.assertFalse(page.exists(menu.accordionContent));
      Assert.assertEquals(page.getCount(menu.accordionItemHeader), expectedCount);
    } else {
      Assert.assertEquals(page.getCount(menu.stepListCircle), expectedCount);
    }
  }

  @Then("^I verify accordion items expand and display text$")
  public void accordionItemFastCheck() {
    Log.info("I verify accordion items expand and display text on get verified modal");
    Assert.assertTrue(menu.accordionOpenCloseAndValidate("", ""));
  }

  @Then("^I verify (accordion|step list) contains text:$")
  public void accordionStepListItemTextMatch(String itemType, DataTable data) {
    Log.info("I verify " + itemType + " contains text:" + data);
    for (String item: page.dtToList(data)) {
      boolean runTest = itemType.equalsIgnoreCase("accordion")
          ? menu.accordionOpenCloseAndValidate(item, "")
          : menu.stepListValidate(item, "");
      if (!runTest) steps.fails.add("FAIL checking: " + item);
    }
    steps.failHandler();
  }

  @Then("^I click and check (accordion|step list) link \"([^\"]*)\"$")
  public void accordionStepListItemCheckLink(String itemType, String link) {
    Log.info("I click and check " + itemType + " link " + link);
    Assert.assertTrue(itemType.equalsIgnoreCase("accordion")
        ? menu.accordionOpenCloseAndValidate("", link)
        : menu.stepListValidate("", link));
  }
}
