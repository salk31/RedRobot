package com.redspr.redrobot;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebDriverRobotTest {

  @Test
  public void testTextMatch() throws Exception {
    WebDriverRobot robot = (WebDriverRobot) getRobot();
    {
      double score = robot.isMatch(new String[]{"some 12356 guff"}, new String[]{"12356"});
      assertTrue(score > 0);
      assertTrue(score < 1);
    }

    {
      double score = robot.isMatch(new String[]{"some 12356 guff"}, new String[]{"12356", "XXX"});
      assertTrue(score == 0);
    }
    robot.close();
  }

}
