package com.redspr.redrobot;

import com.redspr.redrobot.htmlunit.ThreadedHtmlUnitDriverFactory;

public class WebDriverThreadedHtmlUnitCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot(ThreadedHtmlUnitDriverFactory.create());
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }
}
