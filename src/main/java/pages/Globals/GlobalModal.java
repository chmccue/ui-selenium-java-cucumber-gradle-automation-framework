package pages.Globals;

import main.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.tableUtils;

public class GlobalModal extends tableUtils {

  public GlobalModal(WebDriver driver) { super(driver); }

  private final By globalModalConfirm = strToBy("button[data-testid*='confirm']");
  private final By globalModalLoading = strToBy("[data-testid*='loading indicator']");
  public final By globalModalTitle = strToBy("[class*='module_topBar'], .title");

  public final By globalModalContentFacadeOnly = strToBy("[data-testid='dialog']");
  public final By globalModalContent = strToBy(
      "[class*='module_contentWrap'], [aria-modal='true'], .modal");
  public final By globalModalField      = addStrToBy(globalModalContent, " input");
  public final By globalModalHeader     = addStrToBy(globalModalContent, " header");
  private final By globalModalSaveBtn   = strToBy("[class*='save'], [id*='save']");
  private final By globalModalCancelBtn = addStrToBy(globalModalContent,
      " [class*='cancel'], [data-testid*='cancel'], [id*='cancel']");
  private final By globalModalCloseBtn  = strToBy(
      "[class*='closeButton' i], .close-button, .modal .icon-close");
  private final By globalModalNextBtn   = strToBy("[id*='next'], [class*='next']");
  private final By globalModalSubmitBtn = strToBy(
      "[data-testid*='confirm'], [id*='submit'], .submit");
  public final By globalModalBoxChecked = addStrToBy(
      globalModalContent, " [type='checkbox'][value='true'], [type='checkbox'][checked]");

  public final By invalidField = strToBy(
      "[class*='Field-module_negative'], .border-error, input.invalid");

  private By setGlobalModalBtnVar(String button) {
    By methodBtn;
    waitForElement(globalModalContent, 3);
    if (textHas(button, "(save|yes|done)")) methodBtn = globalModalSaveBtn;
    else if (textHas(button, "(cancel|no)")) methodBtn = globalModalCancelBtn;
    else if (textHas(button, "next")) methodBtn = globalModalNextBtn;
    else if (textHas(button, "close")) methodBtn = globalModalCloseBtn;
    else if (textHas(button, "(continue|submit)")) methodBtn = globalModalSubmitBtn;
    else if (textHas(button, "confirm")) methodBtn = globalModalConfirm;
    else methodBtn = combineSelectors(globalModalContent, setHrefVar(button)); // assumes href link
    return methodBtn;
  }

  // after button name, add "**" in string argument to skip the validation of element no
  // longer visible. For example: globalClickModalBtn("cancel**");
  public boolean globalClickModalBtn(String button) {
    By methodBtn = setGlobalModalBtnVar(button);
    boolean clickIt = clickOn(methodBtn);
    // without this timeout, cancel button transition exhibits issues
    if (textHas(button, "(confirm|skip|cancel)")) hardWait(0.2);
    if (textHas(button, "(continue|submit|\\*\\*)")) {
      return clickIt && waitForNotVisible(addStrToBy(methodBtn, ".pending"), 6);
    }
    // enter "skip" with button name to skip button gone validation. eg: button="close - skip val"
    if (textHas(button, "(confirm|skip)")) {
      return clickIt && waitForNotVisible(globalModalLoading, 6);
    }
    return clickIt && waitForNotVisible(methodBtn, 6)
        && (!textHas(button, "close") || waitForNotVisible(globalModalContent, 1));
  }

  public boolean globalModalBtnDisabled(String button) {
    int runAttempts = 4;
    while (!exists(setGlobalModalBtnVar(button)) && runAttempts > 0) {
      hardWait(0.15);
      runAttempts =- 1;
    }
    return !enabled(setGlobalModalBtnVar(button));
  }

  public void closeModalIfOpen() {
    if (exists(globalModalCloseBtn)) {
      globalClickModalBtn("close");
    }
  }

  public boolean closeModalWithEscapeKey(boolean retry) {
    if (exists(globalModalContent)) pressEscBtn();
    if (modalVisibleStatus("found") && retry) {
      hardWait(0.1);
      return closeModalWithEscapeKey(false);
    }
    return modalVisibleStatus("not found");
  }

  public boolean modalVisibleStatus(String foundOrNot, By modalContent) {
    hardWait(0.15);
    return new Loading(driver).waitForLoadSpinnerToDisappear(true)
        && textHas(foundOrNot, "not") != exists(modalContent);
  }

  public boolean modalVisibleStatus(String foundOrNot) {
    return modalVisibleStatus(foundOrNot, globalModalContent);
  }

  // to click multiple checkboxes, enter checkBoxText with commas: checkbox 1, checkbox 2
  public boolean clickCheckBoxes(String checkBoxText) {
    String[] checkBoxes = checkBoxText.replaceAll(", ", ",").split(",");
    boolean noFails = true;
    for (String checkBox : checkBoxes) {
      WebElement checkBoxFound =
          getElementMatch(addStrToBy(globalModalContent, " span"), checkBox);
      if (checkBoxFound == null || !clickOn(checkBoxFound)) {
        Log.addStepLog("finding match or clicking checkbox: " + checkBox, "error");
        noFails = false;
      }
    }
    return noFails;
  }
}