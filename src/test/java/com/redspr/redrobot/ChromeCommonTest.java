package com.redspr.redrobot;

import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    System.setProperty("webdriver.chrome.driver", BinaryUtil.getPathForDriver("chromedriver"));


    ChromeDriver driver = new ChromeDriver();

    Robot robot = new WebDriverRobot(driver);
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }

  @Override
  protected Class getImplClass() {
    return ChromeDriver.class;
  }
}
