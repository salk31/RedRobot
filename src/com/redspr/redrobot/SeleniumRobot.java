package com.redspr.redrobot;

import java.util.Stack;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class SeleniumRobot implements Robot {
    private Stack<String> history = new Stack<String>();

    private Selenium sel;

    public SeleniumRobot() {
        // TODO 00 need to be able to configure this from outside (bean style)
        sel = new DefaultSelenium("localhost", 8080, "*iexplore",
                "http://localhost");

        sel.start();
    }

    public void back() {
        sel.open(history.pop());
        // sel.waitForPageToLoad("10000");
    }

    public void click(String... x) {
        history.push(sel.getLocation());
        sel.click(loc(x));
        try {
            sel.waitForPageToLoad("10000");
        } catch (Throwable t) {
        };
    }

    public int findText(String x) {
        if (sel.isElementPresent("xpath=//node()[text()='" + x + "']")) {
            return 1;
        } else {
            return 0;
        }
    }

    public String get(String... x) {
        // TODO 00 how to tell if select?
        return sel.getValue(loc(x)).replaceAll("\\r", "");
    }

    public String getConfirmation() {
        return sel.getConfirmation();
    }

    public boolean isChecked(String... x) {
        return sel.isChecked(loc(x));
    }

    private String loc(String... x) {
        return "fuzzy=" + x[0];
    }

    private String locKey(String... x) {
        return "fuzzyKey=" + x[0];
    }

    public void open(String path) {
        sel.open(path);
        sel.waitForPageToLoad("10000");
    }

    public void selenium(String x) {
        sel.fireEvent(x, "mousedown");
        sel.fireEvent(x, "mouseup");
    }

    public void set(String... x) {
        String[] n = new String[x.length - 1];
        for (int i = 0; i < n.length; i++) {
            n[i] = x[i];
        }
        String v = x[x.length - 1];
        sel.type(locKey(n), v);
    }
}
