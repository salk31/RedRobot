package com.redspr.redrobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;

abstract public class AbstractCommonTest {

  abstract protected Robot getRobot() throws Exception;

  abstract protected Class getImplClass();

  @Test
  public void testUnwrappedImpl() throws Exception {
    Robot robot = getRobot();

    Object impl = robot.unwrap(getImplClass());
    assertEquals(getImplClass(), impl.getClass());

    robot.close();
  }

  @Test
  public void testAmbiguousForm() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Ambiguous Form");

    assertEquals("textBoxByTitle", robot.get("First bit", "Field 1"));
    robot.set("First bit", "Field 1", "New value for textBoxByTitle");
    assertEquals("New value for textBoxByTitle",
            robot.get("First bit", "Field 1"));
    // TODO 01 how to send "!"?

    assertEquals(" Two", robot.get("Second bit", "Field 1"));
    assertEquals("textareaByTitle", robot.get("First bit", "Field 2"));
    robot.set("First bit", "Field 2", "New text for textareaByTitle");
    assertEquals("New text for textareaByTitle",
    robot.get("First bit", "Field 2"));

    assertEquals("pass", robot.get("Second bit", "Field 2"));

    assertEquals("yestext", robot.get("Second bit", "Yes"));

    robot.close();
  }

  @Test
  public void testNiceForm() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Nice Form");
    assertFalse(robot.isSelected("Checkbox 1"));
    assertTrue(robot.isSelected("Checkbox 2"));

    robot.close();
  }

  @Test
  public void testSimpleForm() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Simple Form");
    assertEquals("textBoxByTitle", robot.get("text 1"));
    assertEquals("textareaByTitle", robot.get("text 2"));
    assertTrue(robot.isSelected("checkbox 3"));
    assertFalse(robot.isSelected("checkbox 4"));
    assertTrue(robot.isSelected("radio 5"));
    assertEquals(" Two", robot.get("select 7"));
    robot.click("Three");
    assertEquals("Three ", robot.get("select 7"));
    assertEquals("pass", robot.get("password 8"));

    robot.close();
  }

  @Test
  public void testHidden() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Hidden");
    try {
      robot.click("Display none");
      fail("Should not be able to find that");
    } catch (NotFoundException ex) {
      // expected
    }
    robot.close();
  }

  @Test
  public void testPartialText() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Partial Text");

    {
      robot.click("foo");
      robot.click("foo clicked", "ok"); // XXX want more confirmation was the
                                        // real one!?
    }

    assertTrue(robot.textExists("foo bar"));
    assertTrue(robot.textExists("foo"));

    assertFalse(robot.textExists("foo bar hidden"));

    robot.close();
  }

  @Test
  public void testIgnore() throws Exception {
    Robot robot = getRobot();

    robot.click("Test ignore");

    assertFalse(robot.textExists("foo bar"));
    
    robot.click("beer");
    robot.click("beer clicked", "ok");
    
    try {
      robot.click("beer", "foo bar");
      fail("Shouldn't have been able to find that");
    } catch (NotFoundException ex) {
      // good
    }
    robot.close();
  }

  @Test
  public void testAmbiguousSelect() throws Exception {
    Robot robot = getRobot();

    robot.click("Test ambiguous select");

    assertEquals("Second", robot.get("Two", "Foo"));

    robot.close();
  }

  // https://code.google.com/p/redrobot/issues/detail?id=30
  @Test
  public void testAmbiguousTable() throws Exception {
      Robot robot = getRobot();

      robot.click("Test ambiguous table");

      String[] chars = new String[]{"a", "b", "c", "d"};
      for (String first : chars) {
          for (String second : chars) {
              robot.click(first, second);
              assertTrue(robot.textExists("alert " + second));
              robot.click("ok");
          }
      }

      robot.close();
  }

  @Test
  public void testDialog() throws Exception {
    Robot robot = getRobot();

    robot.click("Test Dialog");

    robot.click("alert");
    assertTrue(robot.textExists("alert clicked"));
    robot.click("ok");
    assertFalse(robot.textExists("alert clicked"));

    robot.click("alert");
    robot.click("alert clicked", "ok");


    robot.close();
  }

  @Test
  public void testTable() throws Exception {
    Robot robot = getRobot();

    robot.click("Test table");

    robot.click("Orange", "Two", "Foo");
    robot.click("alert orange two", "ok");

    robot.click("Orange", "Three", "Foo");
    robot.click("alert orange three", "ok");

    robot.click("red", "two", "Foo");
    robot.click("alert red two", "ok");

    robot.close();
  }

  @Test
  public void testTableColSpan() throws Exception {
    Robot robot = getRobot();

    robot.click("Test table col span");

    robot.click("bird", "three", "Foo");
    robot.click("alert bird three", "ok");
    //output(robot, "tableColSpanBirdThree");

    robot.close();
  }

  @Test
  public void testTableTotalScore() throws Exception {
    Robot robot = getRobot();

    robot.click("Test table total score");

    robot.click("cat");
    robot.click("alert a", "ok");

    robot.close();
  }

  @Test
  public void testAlertThenWait() throws Exception {
    final WebDriverRobot robot = (WebDriverRobot) getRobot();

    robot.setReadyStrategy(new ReadyStrategy() {
      @Override
      public void waitTillReady() {
        try {
          JavascriptExecutor je = robot.unwrap(JavascriptExecutor.class);
          je.executeScript("var x = 1;");
        } catch (UnhandledAlertException ex) {
          // OK JS is done
        }
      }
    });

    robot.click("Test Dialog");

    robot.click("alert");
    robot.click("ok");
    robot.close();
  }

  @Test
  public void testPerformance() throws Exception {
    final Robot robot = getRobot();
    SimplePerformanceListener listener = new SimplePerformanceListener();
    robot.addListener(listener);

    robot.click("Test Performance");

    robot.setReadyStrategy(new ReadyStrategy() {
        @Override
        public void waitTillReady() {
        }
    });
    int N = 50;

    long t0 = listener.getTotal();
    for (int i = 0; i < N; i++) {
      robot.click("block 200ms");
    }
    long t1 = listener.getTotal();

    long pct = 100 * (t1 - t0) / (N * 200);
    assertTrue("Was " + pct, pct > 100);

    System.out.println("testPerformance " + pct + "%");
    assertTrue("Was " + pct, pct < 120);

    robot.close();
  }

  @Test
  public void testAria() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Aria");

    assertEquals("Correct value", robot.get("Red"));

    robot.close();
  }

  @Test
  public void testConfirm() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Confirm");

    robot.click("confirm");
    robot.click("hello", Robot.OK);
    assertTrue(robot.textExists("result=true"));
    robot.click("result=true", Robot.OK);

    robot.click("confirm");
    robot.click("hello", Robot.CANCEL);
    assertTrue(robot.textExists("result=false"));
    robot.click("result=false", Robot.OK);

    robot.close();
  }

  @Ignore
  @Test
  public void testWatermark() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Watermark");

    String v = "asd" + System.currentTimeMillis();

    robot.set("Box one", v);

    String v2 = robot.get("Box one");
    assertEquals(v, v2);

    robot.close();
  }

  @Test
  public void testReload() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Simple Form");
    assertEquals("textBoxByTitle", robot.get("text 1"));
    assertEquals("textareaByTitle", robot.get("text 2"));

    robot.set("text 1", "Something new");
    assertEquals("Something new", robot.get("text 1"));

    robot.reload();

    assertEquals("textBoxByTitle", robot.get("text 1"));

    robot.close();
  }
}
