package com.redspr.redrobot;

import org.openqa.selenium.firefox.FirefoxDriver;

public class FirefoxCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot();
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }

  @Override
  protected Class getImplClass() {
    return FirefoxDriver.class;
  }
}
