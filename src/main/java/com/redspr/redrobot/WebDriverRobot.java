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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
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
      InputStream is = getClass().getResourceAsStream("/redRobotCore.js");
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
  public void forward() {
    webDriver.navigate().forward();
    readyStrategy.waitTillReady();
  }

  @Override
  public void click(String... x) {
    try {
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
    return webDriver.findElements(By.xpath("//node()[text()='" + x + "']")).size();
  }

  @Override
  public boolean textExists(String... x) {
    return !doFind("RedRobot.isText", x).isEmpty();
  }

  @Override
  public String get(String... x) {
    WebElement e = locKey(x);

    if ("select".equals(e.getTagName())) {
        Select select = new Select(e);
        return select.getFirstSelectedOption().getText();
    }
    WebElement elmt = locKey(x);
    String r = elmt.getAttribute("value");
    if (r == null) {
        r = elmt.getText();
    }
    return r.replaceAll("\\r", "");
  }

  @Override
  public String getConfirmation() {
    throw new RuntimeException("Not supported");
  }

  @Override
  public boolean isSelected(String... x) {
    WebElement e = locCheckable(x);

    return e.isSelected();
  }

  @Override
  public boolean isChecked(String... x) {
    return isSelected(x);
  }

  private List<WebElement> doFind(String cmd, String... args) {
    JavascriptExecutor jse = (JavascriptExecutor) webDriver;
    List<WebElement> y = (List) jse.executeScript(SCRIPT + ";return RedRobot.findBestMatches(arguments, document, " + cmd + ")", args);
    List<WebElement> hits = new ArrayList<WebElement>(y.size());
    for (WebElement we : y) {
      try {
        if (we.isDisplayed()) {
          hits.add(we);
        }
      } catch (Throwable th) {
          // TODO 00 auto switch to frames?
      }
    }
    return hits;
  }


  private WebElement doLocate(String cmd, String... args) {
      JavascriptExecutor jse = (JavascriptExecutor) webDriver;
      List<WebElement> y = (List) jse.executeScript(SCRIPT + ";return RedRobot.findBestMatches(arguments, document, " + cmd + ")", args);
      List<WebElement> hits = new ArrayList<WebElement>(y.size());
      for (WebElement we : y) {
        if (we.isDisplayed()) {
          return we;
        }
      }

    StringBuilder sb = new StringBuilder();
    sb.append("Unable to find ");
    sb.append(cmd);
    for (String a : args) {
      sb.append(", '");
      sb.append(a);
      sb.append("'");
    }

    throw new NotFoundException(sb.toString());
  }

  private WebElement locClickable(String... x) {
    return doLocate("RedRobot.isClickable", x);
  }

  private WebElement locKey(String... x) {
    return doLocate("RedRobot.isKey", x);
  }

  private WebElement locCheckable(String... x) {
    return doLocate("RedRobot.isCheckable", x);
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
