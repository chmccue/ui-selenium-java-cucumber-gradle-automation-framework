package main;

import pages.GlobalNav.TopNavBar;
import pages.Globals.AlertModal;
import org.openqa.selenium.WebDriver;
import utils.stepUtils;

public interface Base {
  WebDriver driver = Driver.startDriver();
  Core coreEnv = new Core(driver);
  TopNavBar navMenu = new TopNavBar(driver);
  AlertModal page = new AlertModal(driver);
  stepUtils steps = new stepUtils();
}
