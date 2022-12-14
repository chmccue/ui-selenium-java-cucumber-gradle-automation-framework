package pages.GlobalNav;

import static utils.matcherUtils.textHas;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

public class AssetPairs {

  public List<String> getAssetList(String enteredAsset) {
    if (textHas(enteredAsset, "/")) return Collections.singletonList(enteredAsset);
    else if (textHas(enteredAsset, "(BCH|DASH|LTC|XDG|XLM|XMR|ZEC)")) enteredAsset = "group1";
    else if (!textHas(enteredAsset, "(XBT|DAI|ETH|USDC|USDT|XRP)")) enteredAsset = "group2";
    return assetPairMap().get(enteredAsset);
  }

  private Map<String, List<String>> assetPairMap() {
    Map<String, List<String>> currencyMap = new HashMap<>();
    List<String> coreAssetGroup1 = Arrays.asList("/EUR", "/USD", "/XBT");
    List<String> coreAssetGroup2 = new ArrayList<>(coreAssetGroup1);
    coreAssetGroup2.add("/ETH");
    currencyMap.put("group1", coreAssetGroup1);
    currencyMap.put("group2", coreAssetGroup2);
    currencyMap.put("XBT", Arrays.asList(
        "/CAD", "/CHF", "/DAI", "/EUR", "/GBP", "/JPY", "/USD", "/USDC", "/USDT"));
    currencyMap.put("DAI", Arrays.asList("/EUR", "/USD", "/USDT"));
    currencyMap.put("ETH", Arrays.asList(
        "/CAD", "/CHF", "/DAI", "/EUR", "/GBP", "/JPY", "/USD", "/USDC", "/XBT"));
    currencyMap.put("USDC", Arrays.asList("/USD", "/EUR", "/USDT"));
    currencyMap.put("USDT", Arrays.asList("/CAD", "/CHF", "/EUR", "/GBP", "/USD"));
    currencyMap.put("XRP", Arrays.asList("/CAD", "/EUR", "/JPY", "/USD", "/XBT"));
    return currencyMap;
  }
}
