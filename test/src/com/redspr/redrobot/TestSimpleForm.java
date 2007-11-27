package com.redspr.redrobot;
import java.net.URL;

import junit.framework.TestCase;



public class TestSimpleForm extends TestCase {

    public void testSelenium() throws Exception {
        testNiceForm(new SeleniumRobot());
        testBasic(new SeleniumRobot());
    }
    
    public void testHtmlUnit() throws Exception {
        testNiceForm(new HtmlUnitRobot());
        testBasic(new HtmlUnitRobot());
    }
    
    private void testNiceForm(Robot robot) throws Exception {
        robot.open(new URL("http://localhost:8080"));
        robot.click("Test Nice Form");
        assertFalse(robot.isChecked("Checkbox 1"));
        assertTrue(robot.isChecked("Checkbox 2"));
    }
    
    private void testBasic(Robot robot) throws Exception {
        robot.open(new URL("http://localhost:8080"));
        robot.click("Test Simple Form");
        assertEquals("textBoxByTitle", robot.get("text 1"));
        assertEquals("textareaByTitle", robot.get("text 2"));
        assertTrue(robot.isChecked("checkbox 3"));
        assertFalse(robot.isChecked("checkbox 4"));
        assertTrue(robot.isChecked("radio 5"));
        assertEquals("Two", robot.get("select 7"));
        assertEquals("pass", robot.get("password 8"));
    }
    

}
