package main;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import com.bettercloud.vault.VaultException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver;
import static main.UserProvisioner.getUser;
import static main.UserProvisioner.releaseUser;
import static utils.matcherUtils.textHas;

public class Core extends Driver {
  public static Map<String, String> meta = new HashMap<>();
  public static UserProvisioner.User user;
  public static UserProvisioner.User envCreds;
  public static boolean chromeTest = false;
  public static boolean firefoxTest = false;
  public static boolean remoteTest = false;
  public static boolean skipStep = false;
  public static String siteUrl;
  public final static String prodEnvs = "(alpha|beta|staging)";

  public Core(WebDriver driver) { super(driver); }

  /**
   * The meta data structure
   */
  public static void loadMetaData() {
    if (meta.isEmpty()) {
      try {
        Log.info("Loading metaData");

        Properties properties = new Properties();
        Properties systemProperties = System.getProperties();
        Map<String, String> env = System.getenv();

        meta.put("url", env.get("SITE_URL"));

        // selenium configuration
        meta.put("driver.type", env.get("SELENIUM_DRIVER_TYPE"));
        meta.put("remote.url", env.get("SELENIUM_REMOTE_URL"));
        remoteTest = textHas(env.get("SELENIUM_DRIVER_TYPE"), "remote");
        meta.put("browser.type", env.get("SELENIUM_BROWSER_TYPE"));
        chromeTest = textHas(env.get("SELENIUM_BROWSER_TYPE"), "chrome");
        firefoxTest = textHas(env.get("SELENIUM_BROWSER_TYPE"), "firefox");
        meta.put("browser.version", env.get("SELENIUM_BROWSER_VERSION"));
        meta.put("maxwait", env.get("SELENIUM_MAX_WAIT_SECONDS_FOR_ELEMENT"));

        // hashicorp
        meta.put("consul.url", env.get("CONSUL_SERVER_URL"));
        meta.put("vault.url", env.get("VAULT_ADDR"));
        meta.put("vault.authtoken", env.get("VAULT_TOKEN"));
        meta.put("vault.opentimeout", env.get("VAULT_OPEN_TIMEOUT"));
        meta.put("vault.readtimeout", env.get("VAULT_READ_TIMEOUT"));

        // user provisioning
        meta.put("user_provisioning.env", env.get("USER_PROVISIONING_ENVIRONMENT"));
        meta.put("user_provisioning.retrylock", env.get("USER_PROVISIONING_RETRY_LOCK"));

        // flags
        // if CONSOLE_LOGGING is not provided, the value defaults to false.
        meta.put("flag.console_logging", env.get("CONSOLE_LOGGING") == null
            ? "false" : env.get("CONSOLE_LOGGING"));

        // get properties file
        ClassLoader myCl = Core.class.getClassLoader();
        properties.load(myCl.getResourceAsStream("environment.properties"));

        // override properties file with system properties
        for (Map.Entry<Object, Object> e : systemProperties.entrySet())
          properties.setProperty(e.getKey().toString(), e.getValue().toString());
        meta.put("env", properties.getProperty("environment"));

        try {
          UserProvisioner userProvisioner = new UserProvisioner(meta);
        } catch (VaultException ve) {
          ve.printStackTrace();
          Log.error(ve.getMessage());
          throw new RuntimeException(ve.getMessage());
        }
        Log.info("Finished loading metaData");

      } catch (IOException e) {
        Log.error(e.getMessage());
      }
    }
  }

  public void open() throws Exception {
    setStandardTimeoutAndResolution();
    open(3);
  }

  public void open(int retry) throws Exception {
    // Note: we use meta.get("url") instead of siteUrl to ensure any site authentication is entered
    open(meta.get("url"), retry, true);
  }

  public void open(String openUrl, int retry, boolean logToReport) throws Exception {
    if (openUrl == null) {
      Log.info("URL not set in Environment. Open browser will fail!");
      throw new Exception("Url not set in Environment");
    }
    try {
      try {
        driver.get(siteAuthenticationHandler(openUrl));
      } catch (org.openqa.selenium.NoSuchSessionException e) {
        driver = Driver.startDriver();
        driver.get(siteAuthenticationHandler(openUrl));
      } catch (TimeoutException e) {
        Log.warn(e.toString());
      }
      // Below replaces any entered site authentication so we do not log username/password
      openUrl = openUrl.replaceAll("://.*?@", "://");
      String openMsg = "Open Browser to: " + openUrl;
      if (textHas(openUrl, "https://" + prodEnvs)) {
        driver.get(openUrl); // re-open the cleaned url to remove credentials in logging
      }
      if (logToReport) Log.addStepLog(openMsg); else Log.info(openMsg);
    } catch (Exception e) {
      Log.error("Caught error opening browser: " + e.toString());
      if (retry > 0) {
        Log.warn("Retrying open browser after driver error");
        retry -= 1;
        open(openUrl, retry, logToReport);
      }
    }
  }

  public void switchToPage(String name) {
    Log.info("Switch to page: " + name);
    try {
      for (String window : driver.getWindowHandles()) {
        driver.switchTo().window(window);
        Log.info("Current window URL: " + driver.getCurrentUrl().replaceAll("://.*?@", "://"));
        if ((driver.getCurrentUrl().contains(siteUrl) && name.equals("Home"))) {
          Log.info("Found page: " + name);
          return;
        }
      }
      Log.info("Page not found: " + name);
    } catch (Exception e) {
      Log.info("Error : " + e + " Switch to " + name + " failed");
    }
  }

  public void closeBrowser() {
    Log.info("Closing Browser.");
    if (!driver.toString().contains("null")) try { driver.quit(); } catch (Exception ignored) {}
  }

  // Closes any extra tabs/windows, then ensures entered window handle is given control.
  // Used in teardown for clean up between test cases.
  public void closeAllTabsExcept(String windowId) {
    try {
      for (String windowHandle : driver.getWindowHandles()) {
        if (!windowHandle.equals(windowId)) {
          driver.switchTo().window(windowHandle);
          driver.close();
        }
      }
      driver.switchTo().window(windowId);
    } catch (WebDriverException ignored) {}
  }

  public void clearAllCookies() {
    Log.info("Clearing all cookies.");
    try {
      driver.manage().deleteAllCookies();
      driver.manage().deleteAllCookies();
    } catch (Exception ignored) {}
  }

  private String siteAuthenticationHandler(String openUrl) {
    if (textHas(openUrl, "https://" + prodEnvs)) {
      Map<String, String> creds = new HashMap<>();
      creds.put("tier", "alpha");
      envCreds = getUser(creds);
      openUrl = "https://" + envCreds.username + ":" + envCreds.password
          + "@" + openUrl.replaceAll("https://", "");
      releaseUser(envCreds);
    }
    return openUrl;
  }
}
