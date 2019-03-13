package com.redspr.redrobot;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class FirefoxCommonTest extends AbstractCommonTest {

  @Override
  protected Robot getRobot() throws Exception {
    System.setProperty("webdriver.gecko.driver", BinaryUtil.getPathForDriver("geckodriver"));

    DesiredCapabilities dc = DesiredCapabilities.firefox();
    dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
    dc.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

    FirefoxDriver driver = new FirefoxDriver(dc);

    Robot robot = new WebDriverRobot(driver);
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

  @Ignore // TODO 01 not working with latest geckodriver, second alert not detected
  @Test
  @Override
  public void testConfirm() throws Exception {
  }
}
