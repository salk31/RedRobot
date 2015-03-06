package com.redspr.redrobot;

import org.junit.Ignore;
import org.junit.Test;
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

  @Ignore // not reliable - 200ms extra pause
  @Test
  @Override
  public void testPerformance() throws Exception {
  }
}
