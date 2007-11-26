package com.redspr.redrobot;
import java.net.URL;

import junit.framework.TestCase;



public class TestSimpleForm extends TestCase {

    public void testSelenium() {
        testBasic(new SeleniumRobot());
    }
    
    public void testHtmlUnit() {
        testBasic(new HtmlUnitRobot());
    }
    
    private void testBasic(Robot robot) {

        robot.open("/b/0/TestSimpleForm.html");

        assertEquals("textBoxByTitle", robot.get("Text 1"));
        assertEquals("textareaByTitle", robot.get("Text 2"));
        assertTrue(robot.isChecked("Checkbox 3"));
        assertFalse(robot.isChecked("Checkbox 4"));
        assertTrue(robot.isChecked("Radio 5"));
        assertEquals("Two", robot.get("Select 7"));
    }
}
