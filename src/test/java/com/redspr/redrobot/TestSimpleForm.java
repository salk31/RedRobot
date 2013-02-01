package com.redspr.redrobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

public class TestSimpleForm {

  private Robot getRobot() throws Exception {
    Robot robot = new WebDriverRobot();
    robot.open(getClass().getResource("/index.html"));
    return robot;
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

    assertEquals("Two", robot.get("Second bit", "Field 1"));
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
    assertFalse(robot.isChecked("Checkbox 1"));
    assertTrue(robot.isChecked("Checkbox 2"));

    robot.close();
  }

  @Test
  public void testSimpleForm() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Simple Form");
    assertEquals("textBoxByTitle", robot.get("text 1"));
    assertEquals("textareaByTitle", robot.get("text 2"));
    assertTrue(robot.isChecked("checkbox 3"));
    assertFalse(robot.isChecked("checkbox 4"));
    assertTrue(robot.isChecked("radio 5"));
    assertEquals("Two", robot.get("select 7"));
    robot.click("Three");
    assertEquals("Three", robot.get("select 7"));
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


  // TODO 00 test ARIA roles
  @Test
  @Ignore
  public void testClickable() throws Exception {
    Robot robot = getRobot();
    robot.click("Test Clickable");

    {
      robot.click("onClick");
      robot.click("onClick clicked", "ok");
    }

    {
      robot.click("onClick");
      robot.click("onClick clicked", "cancel");
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

    robot.close();
  }

  @Test
  public void testAmbiguousSelect() throws Exception {
    Robot robot = getRobot();

    robot.click("Test ambiguous select");

    robot.click("One");

    robot.click("option2", "OK");

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

  // TODO 00 test colspan
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

    //robot.close();
  }

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
