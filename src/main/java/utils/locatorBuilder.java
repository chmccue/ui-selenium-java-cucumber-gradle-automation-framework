package utils;

import static utils.matcherUtils.textHas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.HashSet;
import java.util.Set;

public class locatorBuilder extends windowUtils {

  public locatorBuilder(WebDriver driver) { super(driver); }

  // Along with href string, user can pass in optional wild card surrounded by curly braces to
  // override the default wild card used. Example: setHrefVar("Funding{$}")
  public By setHrefVar(String name) {
    // nameArray removes spaces and "}", and creates array with "{" separator, if it exists.
    String[] nameArray = name.toLowerCase().replaceAll("[ }]", "").split("\\{");
    name = nameArray[0];
    // if no wild card specified, defaults to "*". Other wild card options: "^","$"
    String hrefWildCard = nameArray.length > 1 ? nameArray[1] : "*";
    if (textHas(name, "(intermediate|advanced)")) name = name.substring(0, 3);
    else if (textHas(name, "companypro"))          name = "trade";
    else if (textHas(name, "getverified"))        name = "verify";
    else if (textHas(name, "notifications"))      name = "notify";
    else if (textHas(name, "neworder"))           name = "new-order";
    else if (textHas(name, "signin"))             name = "sign-in";
    else if (textHas(name, "terms"))              name = "legal";
    return cssBuilder("href" + hrefWildCard, name);
  }

  public By cssBuilder(String attributeType, String value) {
    return By.cssSelector("[" + attributeType + "='" + value + "' i]" );
  }

  // addBase adds base locator to existing Set (must consist of By locators) if base element is not
  // already in Set locators. Allows us to use generic/cleaner vars with less static duplication.
  // Ex: addBaseToBySet(addBase=By.css("foo"), existingSet={By.css("bar")}) -> By.css("foo bar")
  protected Set<By> addBaseToBySet(By addBase, Set<By> existingSet) {
    Set<By> newContent = new HashSet<>();
    By toAdd;
    for (By element : existingSet) {
      // if "addBase" is already in HashSet element, it doesn't get added again
      toAdd = byToStr(element).contains(byToStr(addBase))
          ? element : combineSelectors(addBase, element);
      newContent.add(toAdd);
    }
    return newContent;
  }

  // Adds string to end of By selector. If addString is sub section of locator, make sure to
  // include a space at start. Example: addString=" option" (note space after equal sign)
  public By addStrToBy(By locator, String addString) {
    if (textHas(locator.toString(), "xpath")) return By.xpath(byToStr(locator) + addString);
    StringBuilder cssLoc = new StringBuilder();
    for (String start : byToStr(locator).split(", ?")) {
      for (String end : addString.split(", ?")) {
        // we add a space before each end if addString is entered with a space.
        if (addString.startsWith(" ")) {
          cssLoc.append(start).append(" ").append(end).append(", ");
        } else {
          cssLoc.append(start).append(end).append(", ");
        }
      }
    }
    return strToBy(cssLoc.toString().replaceAll(", ?$", ""));
  }

  // Combines 2 css or xpath selectors.
  // Example: parent=By.cssSelector("#foo"), child=By.cssSelector(".bar")
  // returns By.cssSelector("#foo .bar")
  public By combineSelectors(By parent, By child) {
    return addStrToBy(parent, " " + byToStr(child));
  }
}
