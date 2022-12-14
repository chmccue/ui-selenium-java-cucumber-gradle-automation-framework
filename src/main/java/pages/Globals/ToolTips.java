package pages.Globals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;

public class ToolTips extends GlobalModal {

  public ToolTips(WebDriver driver) { super(driver); }

  public final By toolTip = By.cssSelector("[role='tooltip']");
  public final By toolTipError = addStrToBy(toolTip, " .error");
  protected final By toolTipLeft = By.cssSelector(".rc-tooltip-placement-left:not(.rc-tooltip-hidden)");

  public boolean checkRCToolTipMessage(List<String> toolTipData) {
    By toolTipType;

    switch (toolTipData.get(0).toLowerCase()) {
      case "error":
        toolTipType = toolTipError;
        break;
      case "info":
        toolTipType = toolTipLeft;
        break;
      default:
        toolTipType = toolTip;
    }
    waitForElement(toolTipType, 3);
    if (toolTipData.size() == 1) return getElementText(toolTipType).length() > 0;
    return regexElementMatch(toolTipType, toolTipData.get(1));
  }

}