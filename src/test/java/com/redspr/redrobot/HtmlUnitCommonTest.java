package com.redspr.redrobot;


public class HtmlUnitCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new HtmlUnitRobot();
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }
}
