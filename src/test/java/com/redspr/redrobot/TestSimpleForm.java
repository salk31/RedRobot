package com.redspr.redrobot;
import java.net.URL;

import junit.framework.TestCase;


public class TestSimpleForm extends TestCase {

    public void testSelenium() throws Exception {
        testAmbiguousForm(new WebDriverRobot());
        testNiceForm(new WebDriverRobot());
        testSimpleForm(new WebDriverRobot());
    }

//    public void testHtmlUnit() throws Exception {
//        testAmbiguousForm(new HtmlUnitRobot());
//        testNiceForm(new HtmlUnitRobot());
//        testSimpleForm(new HtmlUnitRobot());
//    }
// TODO 00 need to shut down browsers!
    private void testAmbiguousForm(Robot robot) throws Exception {
        robot.open(new URL("http://localhost:8185"));
        robot.click("Test Ambiguous Form");

        assertEquals("textBoxByTitle", robot.get("First bit", "Field 1"));
        robot.set("First bit", "Field 1", "New value for textBoxByTitle");
        assertEquals("New value for textBoxByTitle", robot.get("First bit", "Field 1"));
// TODO 01 how to send "!"?

        assertEquals("Two", robot.get("Second bit", "Field 1"));
        assertEquals("textareaByTitle", robot.get("First bit", "Field 2"));
        robot.set("First bit", "Field 2", "New text for textareaByTitle");
        assertEquals("New text for textareaByTitle", robot.get("First bit", "Field 2"));


        assertEquals("pass", robot.get("Second bit", "Field 2"));

        assertEquals("yestext", robot.get("Second bit", "Yes"));
    }

    private void testNiceForm(Robot robot) throws Exception {
        robot.open(new URL("http://localhost:8185"));
        robot.click("Test Nice Form");
        assertFalse(robot.isChecked("Checkbox 1"));
        assertTrue(robot.isChecked("Checkbox 2"));
    }

    private void testSimpleForm(Robot robot) throws Exception {
        robot.open(new URL("http://localhost:8185"));
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
    }
}
