package com.redspr.redrobot;

import org.junit.Ignore;
import org.openqa.selenium.chrome.ChromeDriver;

@Ignore
public class ChromeCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot(new ChromeDriver());
    robot.open(getClass().getResource("/index.html"));

    return robot;
  }

  @Override
  protected Class getImplClass() {
    return ChromeDriver.class;
  }
}
