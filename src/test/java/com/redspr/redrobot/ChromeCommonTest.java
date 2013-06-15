package com.redspr.redrobot;

import org.openqa.selenium.chrome.ChromeDriver;

public class ChromeCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot(new ChromeDriver());
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }
}
