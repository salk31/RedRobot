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

        robot.open("/TestSimpleForm.html");

        assertEquals("textBoxByTitle", robot.get("text 1"));
        assertEquals("textareaByTitle", robot.get("text 2"));
        assertTrue(robot.isChecked("checkbox 3"));
        assertFalse(robot.isChecked("checkbox 4"));
        assertTrue(robot.isChecked("radio 5"));
        assertEquals("Two", robot.get("select 7"));
        assertEquals("pass", robot.get("password 8"));
    }
}
