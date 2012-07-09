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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class WebDriverRobot implements Robot {
  private WebDriver webDriver;

  private String SCRIPT;

  private ReadyStrategy readyStrategy = new SleepReadyStrategy();

  public WebDriverRobot() {
    this(new FirefoxDriver());
  }

  public WebDriverRobot(WebDriver webDriver2) {
    this.webDriver = webDriver2;

    try {
      InputStream is = getClass().getResourceAsStream("/webDriver.js");
      ByteArrayOutputStream boas = new ByteArrayOutputStream();
      byte[] buff = new byte[2048];
      int len;
      while ((len = is.read(buff)) > 0) {
        boas.write(buff, 0, len);
      }
      SCRIPT = boas.toString();
    } catch (IOException ex) {
      throw new RuntimeException("Unable to read script" , ex);
    }
  }

  @Override
  public void back() {
    webDriver.navigate().back();
    readyStrategy.waitTillReady();
  }

  @Override
  public void click(String... x) {
    try {
      // TODO 01 unit test for this
      Alert alert = webDriver.switchTo().alert();
      if (x.length > 1) {
          if (!alert.getText().equalsIgnoreCase(x[0])) {
              throw new IllegalArgumentException("Alert text did not match '" + x[0] + "'");
          }
          String button = x[x.length - 1];
          if ("OK".equalsIgnoreCase(button)) {
              alert.accept();
          } else if ("Cancel".equalsIgnoreCase(button)) {
              alert.dismiss();
          } else {
              throw new IllegalArgumentException("It was alert so the last locator should be 'OK' or 'Cancel'");
          }
      }
      return; // XXX hmmm
    } catch (NoAlertPresentException ex) {
      // fine
    }

    locClickable(x).click();
    readyStrategy.waitTillReady();
  }

  @Override
  public int findText(String x) {
    if (false) { // TODO 00 sel.isElementPresent("xpath=//node()[text()='" + x + "']")) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public String get(String... x) {
    WebElement e = locKey(x);

    if ("select".equals(e.getTagName())) {
        Select select = new Select(e);
        return select.getFirstSelectedOption().getText();
    }

    return locKey(x).getAttribute("value").replaceAll("\\r", "");
  }

  @Override
  public String getConfirmation() {
    return ""; // TODO 00 sel.getConfirmation();
  }

  @Override
  public boolean isChecked(String... x) {
    WebElement e = locCheckable(x);

    return e.isSelected();
  }

  private WebElement doLocate(String cmd, String... args) {
      JavascriptExecutor jse = (JavascriptExecutor) webDriver;
      Object y = jse.executeScript(SCRIPT.replace("**COMMAND**", cmd), args);

      return (WebElement) y;
  }

  private WebElement locClickable(String... x) {
    return doLocate("redrobotIsClickable", x);
  }

  private WebElement locKey(String... x) {
    return doLocate("redrobotIsKey", x);
  }

  private WebElement locCheckable(String... x) {
    return doLocate("redrobotIsCheckable", x);
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
     webDriver.get(url.toString());
     readyStrategy.waitTillReady();
  }

  @Override
  public void set(String... x) {
    String[] n = new String[x.length - 1];
    for (int i = 0; i < n.length; i++) {
      n[i] = x[i];
    }
    String v = x[x.length - 1];
    WebElement e = this.locKey(n);
    e.clear();
    e.sendKeys(v);
    readyStrategy.waitTillReady();
  }

  @Override
  public void close() {
    webDriver.quit();
  }
}
