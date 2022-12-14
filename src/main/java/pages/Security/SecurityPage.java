package pages.Security;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.Arrays;
import java.util.HashSet;

public class SecurityPage extends TfaSetUp {

  public SecurityPage(WebDriver driver) { super(driver); }

  public final By securityToggle = strToBy("[class*='switch']");
  public final By securityToggleOn = addStrToBy(securityToggle, "[checked]");
  private final By securityMethodSet = strToBy("[data-testid='authentication method']");

  // onOrOff takes one of: [on, off, true, false]
  public boolean checkTfaToggleStatus(String section, String onOrOff) {
    waitForElement(setSecurityTfa(section), 5, true, 2);
    if (textHas(section, "api")) {
      boolean apiRowCheck = exists(setSecurityTfa("api"))
          && tableRowContains(setSecurityTfa("api"), new HashSet<>(Arrays.asList(
          section.toLowerCase().replaceFirst("api:?", ""), "edit", "change method")));
      return textHas(onOrOff, "(on|true)") == apiRowCheck;
    }
    By methodSet = combineSelectors(setSecurityTfa(section), securityMethodSet);
    return textHas(onOrOff, "(on|true)")
        ? waitForElement(methodSet, 2) : waitForNotVisible(methodSet,2);
  }

  public boolean checkTfaMethodText(String section, String expectedText) {
    waitForElement(setSecurityTfa(section), 5, true, 2);
    if (textHas(section, "api")) {
      String keyName = section.toLowerCase().replaceFirst("api:?", "");
      return tableRowContains(setSecurityTfa("api"), new HashSet<>(Arrays.asList(
          keyName, expectedText)));
    }
    return elementHas(combineSelectors(setSecurityTfa(section), securityMethodSet), expectedText);
  }

  public boolean clickToggleOpensTfaModal(String section, boolean on) {
    By tfaModal = on ? modalSetUpTfa : modalDisableTfa;
    if (textHas(section, "api")) {
      return clickOnTableRowLoc(setSecurityTfa("api"),
          section.toLowerCase().replaceFirst("api:?", ""),
          securityToggle) && waitForElement(tfaModal, 2);
    } else if (textHas(section, "login")) {
      return clickOnLoginSection("activate now");
    }
    By toggle = combineSelectors(setSecurityTfa(section), securityToggle);
    return clickOnAndWait(toggle, tfaModal, 1);
  }

  // changeType takes one of: [activate now, change password, change method]
  public boolean clickOnLoginSection(String changeType) {
    return clickOnText(byToStr(setSecurityTfa("login")) + " span", changeType);
  }

  // Section takes one of: [login, funding, trading, masterkey, api]
  By setSecurityTfa(String section) {
    if (textHas(section, "login")) section = "sign-in";
    else if (textHas(section, "api")) section = "api";
    return cssBuilder("data-testid", "section-" + section.replace(" ", ""));
  }

  public boolean clickToggleAndTurnOffTfa(String section, boolean retry) {
    boolean clickToggle = textHas(section, "any")
        ? clickOn(securityToggleOn) : clickToggleOpensTfaModal(section, false);
    boolean clickAndClose = globalClickModalBtn("confirm") && modalVisibleStatus("not found");
    if ((!clickToggle || !clickAndClose) && retry) {
      pageRefresh();
      return clickToggleAndTurnOffTfa(section, false);
    }
    return clickAndClose;
  }
}
