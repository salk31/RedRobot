package com.redspr.redrobot;

import com.gargoylesoftware.htmlunit.WebClient;


public class HtmlUnitCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new HtmlUnitRobot();
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }

  @Override
  protected Class getImplClass() {
    return WebClient.class;
  }
}
