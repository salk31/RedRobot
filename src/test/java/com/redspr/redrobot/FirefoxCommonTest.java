package com.redspr.redrobot;

import org.openqa.selenium.firefox.FirefoxDriver;

public class FirefoxCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot(new FirefoxDriver());
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }
}
