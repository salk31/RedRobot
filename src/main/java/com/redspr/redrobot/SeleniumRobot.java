/*
 * Copyright 2007 Sam Hough
 *
 * This file is part of REDROBOT.
 *
 * REDROBOT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * REDROBOT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDROBOT.  If not, see <http://www.gnu.org/licenses/>.
 */
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

  public SeleniumRobot() {
    this("*firefox");
  }

  private Selenium getSelenium(URL url) {
    // XXX do this if proto/domain/port changed
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
    // XXX need to be able to configure this from outside (bean style)
  }

  @Override
  public void back() {
    sel.open(history.pop());
    // sel.waitForPageToLoad("10000");
  }

  @Override
  public void forward() {
      throw new RuntimeException("Not implemented yet.");
  }

  @Override
  public void reload() {
      throw new RuntimeException("Not implemented yet.");
  }

  @Override
  public void click(String... x) {
    history.push(sel.getLocation()); // XXX may not be a page

    try {
      sel.select(locClickable(x), x[x.length - 1]);
      return;
    } catch (Throwable t) {
      // XXX
    }
    sel.click(locClickable(x));

    try {
      sel.waitForPageToLoad("10000");
    } catch (Throwable t) {
    }
  }

  @Override
  public int findText(String x) {
    if (sel.isElementPresent("xpath=//node()[text()='" + x + "']")) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public String get(String... x) {
    try {
      return sel.getSelectedLabel(locKey(x));
    } catch (SeleniumException ex) {
      ex.printStackTrace();
      // not a select thing
      // XXX want nicer way

    }
    //System.out.println("NOT SELECT " + x[0]);
    return sel.getValue(locKey(x)).replaceAll("\\r", "");
  }

  @Override
  public String getConfirmation() {
    return sel.getConfirmation();
  }

  @Override
  public boolean isSelected(String... x) {
    return sel.isChecked(locCheckable(x));
  }

  @Override
  public boolean isChecked(String... x) {
      return isSelected(x);
  }

  private String locClickable(String... x) {
    return "fuzzyClickable=" + escape(x);
  }

  private String locKey(String... x) {
    return "fuzzyKey=" + escape(x);
  }

  private String locCheckable(String... x) {
    return "fuzzyCheckable=" + escape(x);
  }

  private static String escape(String... x) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < x.length; i++) {
      if (i > 0)
        sb.append(',');
      sb.append(x[i]);
    }
    return sb.toString();
  }

  @Override
  public void open(URL url) {
    getSelenium(url);
    sel.open(url.getPath());
    sel.waitForPageToLoad("10000");
  }

  public void selenium(String x) {
    sel.fireEvent(x, "mousedown");
    sel.fireEvent(x, "mouseup");
  }

  @Override
  public void set(String... x) {
    String[] n = new String[x.length - 1];
    for (int i = 0; i < n.length; i++) {
      n[i] = x[i];
    }
    String v = x[x.length - 1];
    try {
      sel.select(locKey(n), v);
      return;
    } catch (SeleniumException ex) {
      // XXX log?!?
    }
    sel.type(locKey(n), v);
  }

  @Override
  public void close() {
  }

  @Override
  public boolean textExists(String... x) {
    throw new RuntimeException("Not implemented");
  }


  @Override
  public void setReadyStrategy(ReadyStrategy p) {
  }
  
  @Override
  public void addListener(RobotListener listener) {
    throw new RuntimeException("Robot.addListener not implemented");
  }
}
