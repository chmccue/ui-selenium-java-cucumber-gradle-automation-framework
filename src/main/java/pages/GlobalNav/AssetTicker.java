package pages.GlobalNav;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.Globals.Search;

public class AssetTicker extends Search {

  public AssetTicker(WebDriver driver) {
    super(driver);
  }

  private final By assetPairMenu = strToBy("[class*='pairSelector']");
  private final By assetPairFilterItem = strToBy("[class*='selectionBar_'] button");
  private final By assetPairTextBtn = strToBy(".Table .button-reset");
  private final By assetPairSelected = strToBy("[aria-label='Selected pair']");
  private final By assetTickerMenu = strToBy("[class*='selectedPairData']");
  private final By assetTickerValue = strToBy("[class*='pairDataValue']");

  // takes pair separated by slash (XBT/EUR) or menu and pair slash separated by ">" (XBT>DASH/XBT)
  public boolean assetTickerCurrencyMenu(String assetOption) {
    return assetTickerCurrencyMenu(assetOption, true);
  }

  private boolean assetTickerCurrencyMenu(String assetOption, boolean retry) {
    assetOption = assetOption.toUpperCase().replace(" ", "");
    String assetToClick =  assetOption.split(">")[assetOption.split(">").length - 1];
    boolean openMenuAndEnterSearchTerm = openAssetPairMenu()
        && enterTextInSearchField(assetToClick.replace("/", ""));
    hardWait(0.1);
    boolean selectResult = clickOn(assetPairTextBtn) && waitForNotVisible(assetPairFilterItem, 2)
        && regexElementMatch(assetPairSelected, assetToClick);
    if ((!openMenuAndEnterSearchTerm || !selectResult) && retry) {
      return assetTickerCurrencyMenu(assetOption, false);
    }
    return openMenuAndEnterSearchTerm && selectResult;
  }

  public boolean openAssetPairMenu() {
    return exists(assetPairFilterItem) || (tickerBarMenuReady(3)
        && clickOnAndWait(assetPairMenu, assetPairFilterItem, 2)
        && clickOnText(byToStr(assetPairFilterItem), "All"));
  }
  private boolean tickerBarMenuReady(int retry) {
    if (!waitForElement(assetTickerMenu, 5)) return false;
    boolean checkForText = waitUntilTextNotPresent(assetTickerValue, "-", 5, "all");
    if (!checkForText && retry  > 0) {
      pageRefresh();
      return tickerBarMenuReady(retry - 1);
    }
    return checkForText;
  }

  public String setAssetPath(String enteredAsset, String assetPair) {
    if (textHas(assetPair, "^/")) {
      assetPair = enteredAsset + ">" + enteredAsset + assetPair;
    } else if (textHas(assetPair, "/$")) {
      assetPair = enteredAsset + ">" + assetPair + enteredAsset;
    }
    return assetPair;
  }
}
