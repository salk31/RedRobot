package com.redspr.redrobot;
import junit.framework.TestCase;



public class TestHtmlUnit extends TestCase {

    
    public void testBasic() {
        Robot robot = new HtmlUnitRobot();
        robot.open("http://www.google.co.uk");
        robot.click("Images");
        robot.set("Google Search", "flower");
        assertEquals("flower", robot.get("Google Search"));
        robot.click("Google Search");
    }
}
