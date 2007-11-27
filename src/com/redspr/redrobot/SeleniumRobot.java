package com.redspr.redrobot;

import java.net.URL;
import java.util.Stack;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumRobot implements Robot {
    private Stack<String> history = new Stack<String>();

    private Selenium sel;

    private String browserString;

    // private String base = "http://localhost:8080";

    public SeleniumRobot() {
        this("*firefox");
    }

    private Selenium getSelenium(URL url) {
        // TODO 00 only do this if proto/domain/port changed
        if (sel == null) {
            String x = url.getProtocol() + "://" + url.getHost() + ":"
                    + url.getPort();

            sel = new DefaultSelenium("localhost", 4444, browserString, x);
            sel.start();
        }
        return sel;
    }

    public SeleniumRobot(String browserString2) {
        this.browserString = browserString2;
        // TODO 00 need to be able to configure this from outside (bean style)

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
        }
        ;
    }

    public int findText(String x) {
        if (sel.isElementPresent("xpath=//node()[text()='" + x + "']")) {
            return 1;
        } else {
            return 0;
        }
    }

    public String get(String... x) {
        try {
            return sel.getSelectedLabel(loc(x));
        } catch (SeleniumException ex) {
            // not a select thing
            // XXX want nicer way
        }
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

    public void open(URL url) {
        getSelenium(url);
        sel.open(url.getPath());
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
