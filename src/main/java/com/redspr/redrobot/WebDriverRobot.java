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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;

public class WebDriverRobot implements Robot {
  private final WebDriver webDriver;

  private String SCRIPT;

  private ReadyStrategy readyStrategy = new SleepReadyStrategy();

  public WebDriverRobot() {
    this(new FirefoxDriver());
  }

  public WebDriverRobot(WebDriver webDriver2) {
    this.webDriver = webDriver2;

    Capabilities caps = ((RemoteWebDriver) webDriver).getCapabilities();
    String browserName = caps.getBrowserName();
    String browserVersion = caps.getVersion();
    System.out.println("Browser details: " + browserName + " " + browserVersion);

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
  public void reload() {
    webDriver.navigate().refresh();
    readyStrategy.waitTillReady();
  }

  double isMatch(String[] source, String[] input) {
    ScriptEngineManager factory = new ScriptEngineManager();
    // create a JavaScript engine
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    try {
      engine.eval(SCRIPT);
      engine.put("source", source);
      engine.put("input", input);
      Double rawResult = (Double) engine.eval("RedRobot.multiTextMatch(source, input)");
      return rawResult.doubleValue();
    } catch (ScriptException ex) {
        throw new RuntimeException(ex);
    }
  }

  @Override
  public Result click(String... x) {
    if (x == null || x.length == 0) {
        throw new RuntimeException("At least one selector required");
    }
    try {
      Alert alert = webDriver.switchTo().alert();

      double scoreOk = isMatch(new String[]{alert.getText(), "OK"}, x);
      double scoreCancel = isMatch(new String[]{alert.getText(), "Cancel"}, x);

      if (scoreOk > scoreCancel) {
          alert.accept();
      } else if (scoreOk < scoreCancel) {
          alert.dismiss();
      } else {
          throw new IllegalArgumentException("Alert text did not match '" + x[0] + "'");
      }
      return null;
    } catch (NoAlertPresentException ex) {
      // fine, was no alert

      locClickable(x).click();

      readyStrategy.waitTillReady();
      return new WebDriverResult(this, last);
    }
  }

  @Override
  public int findText(String x) {
    return webDriver.findElements(By.xpath("//node()[text()='" + x + "']")).size();
  }

  @Override
  public boolean textExists(String... x) {
    try {
      Alert alert = webDriver.switchTo().alert();
      // XXX magic
      return isMatch(new String[]{alert.getText(), "OK", "Cancel"}, x) > 0;
    } catch (NoAlertPresentException ex) {
      return !doFind("RedRobot.isText", x).isEmpty();
    }
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

  void debug(List<WebElement> xxx) {

      int count = 1;
      for (WebElement elmt : xxx) {
      try {
      JavascriptExecutor jse2 = (JavascriptExecutor) webDriver;
      Object rawResult2 = jse2.executeScript(SCRIPT
              + ";return RedRobot.addDebug(document, arguments[0], arguments[1], arguments[2], arguments[3])",
              new Object[]{elmt, elmt.getLocation().getX(), elmt.getLocation().getY(), count});
      count++;
      } catch (Throwable tg) {
          tg.printStackTrace();

      }
       if (count > 4) break;
      }
  }

   void clearDebug() {

      try {
      JavascriptExecutor jse2 = (JavascriptExecutor) webDriver;
      Object rawResult2 = jse2.executeScript(SCRIPT
              + ";return RedRobot.clearDebug(document)");

      } catch (Throwable tg) {
          tg.printStackTrace();

      }

  }

  private List<WebElement> last;

  private WebElement doLocate(String cmd, String... args) {
    JavascriptExecutor jse = (JavascriptExecutor) webDriver;
    Object rawResult = jse.executeScript(SCRIPT
            + ";return RedRobot.findBestMatches(arguments, document, " + cmd + ")",
            args);

    if (!(rawResult instanceof List)) {
      throw new RuntimeException("Expected a list but got '" + rawResult + "'");
    }

    List<WebElement> y = (List) rawResult;
    last = y;
    WebElement got = null;
    //debug(y);
    for (WebElement we : y) {
      try {
        if (we.isDisplayed()) {
          got = we;
          break;
        }
      } catch (Throwable ex) {
        // ignore, log for debug/performance?
      }
    }
    //debug(y);
    if (got == null) {
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
    return got;
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
    try {
      e.clear();
      e.sendKeys(v);
    } catch (WebDriverException ex) {
      throw new RuntimeException("Failed trying to click on name='" + e.getTagName() + "' text='" + e.getText() + "'", ex);
    }
    readyStrategy.waitTillReady();
  }

  @Override
  public void close() {
    webDriver.quit();
  }

  @Override
  public void setReadyStrategy(ReadyStrategy p) {
    this.readyStrategy = p;
  }
}
