package pages.Globals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.clickUtils;
import java.util.List;

public class InfoMenu extends clickUtils {

  public InfoMenu(WebDriver driver) { super(driver); }

  public final By accordionContent = By.cssSelector(".accordion");
  public final By accordionItemHeader = By.cssSelector(".accordion-item");
  private final By accordionItemText = By.cssSelector(".accordion-item-content:not(.closed)");

  private final By stepListContent = By.cssSelector(".step-list");
  public final By stepListCircle = By.cssSelector(".step-list-progress [role='listitem']");

  public boolean accordionOpenCloseAndValidate(String checkSpecificText, String linkHref) {
    List<WebElement> items = getElements(accordionItemHeader);
    boolean checkCopy = false;
    boolean clickLink = false;
    for (WebElement element: items) {
      boolean expandItem = clickOn(element);
      hardWait(0.2);
      boolean itemExpanded = waitForElement(accordionItemText, 2);
      if (!expandItem || !itemExpanded
          || getElementText(accordionItemText).length() == 0) return false;
      if (!checkSpecificText.isEmpty()) {
        checkCopy = elementHas(accordionContent, checkSpecificText);
      }
      if (!linkHref.isEmpty()) clickLink =
          clickOnAndVerifyUrl(combineSelectors(accordionContent, setHrefVar(linkHref)), linkHref);
      boolean collapseItem = clickOn(element);
      hardWait(0.2);
      boolean itemCollapsed = waitForNotVisible(accordionItemText, 2);
      if (!collapseItem || !itemCollapsed) return false;
      if (checkCopy || clickLink) break;
    }
    return (checkCopy || checkSpecificText.isEmpty()) && (clickLink || linkHref.isEmpty());
  }

  public boolean stepListValidate(String checkSpecificText, String linkHref) {
    List<WebElement> items = getElements(stepListCircle);
    boolean checkCopy = false;
    boolean clickLink = false;
    for (WebElement element: items) {
      boolean scrollCircle = clickOn(element);
      hardWait(0.2);
      if (!scrollCircle) return false;
      if (!checkSpecificText.isEmpty()) {
        checkCopy = elementHas(stepListContent, checkSpecificText);
      }
      if (!linkHref.isEmpty()) clickLink =
          clickOnAndVerifyUrl(combineSelectors(stepListContent, setHrefVar(linkHref)), linkHref);
      if (checkCopy || clickLink) break;
    }
    return (checkCopy || checkSpecificText.isEmpty()) && (clickLink || linkHref.isEmpty());
  }
}
