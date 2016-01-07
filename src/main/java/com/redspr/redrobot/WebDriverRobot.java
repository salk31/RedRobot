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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class WebDriverRobot implements Robot {

  private static final Logger LOGGER =
            Logger.getLogger(WebDriverRobot.class.getName());

  /**
   * Wrapped implementation.
   */
  private  WebDriver webDriver;

  /**
   * JavaScript source.
   */
  private String SCRIPT;

  /**
   * Swappable ready strategy.
   */
  private ReadyStrategy readyStrategy = new SleepReadyStrategy();

  /**
   * Listeners to be called in order.
   */
  private final List<RobotListener> listeners = new ArrayList<RobotListener>();

  public WebDriverRobot() {
    DesiredCapabilities dc = DesiredCapabilities.firefox();
    dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
    WebDriver ff = new FirefoxDriver(dc);

    init(ff);
  }

 public WebDriverRobot(WebDriver webDriver2) {
   init(webDriver2);
 }

  public void init(WebDriver webDriver2) {
    this.webDriver = webDriver2;

    URL url = getClass().getResource("/redRobotCore.js");

    try {
      SCRIPT = Resources.toString(url, Charsets.UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Unable to read script" , ex);
    }
  }

  @Override
  public void back() {
    webDriver.navigate().back();
    waitTillReady();
  }

  @Override
  public void forward() {
    webDriver.navigate().forward();
    waitTillReady();
  }

  @Override
  public void reload() {
    webDriver.navigate().refresh();
    waitTillReady();
  }

  double isMatch(String[] source, String[] input) {
    ScriptEngineManager factory = new ScriptEngineManager();
    // create a JavaScript engine
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    try {
      engine.eval(SCRIPT);
      engine.put("source", source);
      engine.put("input", input);
      Number rawResult = (Number) engine.eval("RedRobot.multiTextMatch(source, input)");
      return rawResult.doubleValue();
    } catch (ScriptException ex) {
        throw new RuntimeException(ex);
    }
  }

  @Override
  public void click(String... x) {
    if (x == null || x.length == 0) {
        throw new RuntimeException("At least one selector required");
    }

    try {
      WebElement elmt = locClickable(x);
      for (RobotListener l : listeners) {
        l.actionStart();
      }
      elmt.click();
      for (RobotListener l : listeners) {
        l.actionEnd();
      }
    } catch (UnhandledAlertException ex) {
      Alert alert = webDriver.switchTo().alert();

      double scoreOk = isMatch(new String[]{alert.getText(), OK}, x);
      double scoreCancel = isMatch(new String[]{alert.getText(), CANCEL}, x);

      if (scoreOk > scoreCancel) {
        alert.accept();
      } else if (scoreOk < scoreCancel) {
        alert.dismiss();
      } else {
        throw new IllegalArgumentException("Asked to click on '"
            + x[0] + "' but was an Alert with text '"
            + alert.getText() + "'");
      }
    }

    waitTillReady();
  }

  /**
   * Use the provided wait strategy and call listeners.
   */
  private void waitTillReady() {
    for (RobotListener l : listeners) {
      l.waitTillReadyStart();
    }
    readyStrategy.waitTillReady();
    for (RobotListener l : listeners) {
      l.waitTillReadyEnd();
    }
  }

    @Override
    public int findText(String x) {
        return webDriver.findElements(By.xpath("//node()[text()='" + x + "']"))
                .size();
    }

  @Override
  public boolean textExists(String... x) {
    try {
      String[] n = allButLast(x);
      String v = x[x.length - 1];
      try {
        locText(n , v);
      } catch (NotFoundException ex2) {
        return false;
      }
      return true;
    } catch (UnhandledAlertException ex) {
      Alert alert = webDriver.switchTo().alert();

      return isMatch(new String[]{alert.getText(), OK, CANCEL}, x) > 0;
    }
  }

  @Override
  public String get(String... x) {
    WebElement elmt = locKey(x);

    if ("select".equals(elmt.getTagName())) {
        Select select = new Select(elmt);
        return select.getFirstSelectedOption().getText();
    }

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

  private WebElement doLocate(String cmd, Object cmdArg, String[] args) {
    JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        Object rawResult = jse.executeScript(SCRIPT
                + ";return RedRobot.findBestMatches(document, " + cmd
                + " , arguments[0], arguments[1])",
                new Object[] {cmdArg, args });

    if (!(rawResult instanceof List)) {
      throw new RuntimeException("Expected a list but got '" + rawResult + "'");
    }

    for (RobotListener l : listeners) {
      l.locatorStart();
    }

    List<WebElement> y = (List) rawResult;
    for (WebElement we : y) {
      try {
        if (we.isDisplayed()) {
          for (RobotListener l : listeners) {
            l.locatorEnd(new LocatorResultImpl(we));
          }
          return we;
        }
      } catch (Throwable ex) {
        LOGGER.log(Level.WARNING, "Locator failed", ex);
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
    for (RobotListener l : listeners) {
        l.locatorEnd(null);
    }

    throw new NotFoundException(sb.toString());
  }

  private WebElement locClickable(String... x) {
    return doLocate("RedRobot.isClickable", null, x);
  }

  private WebElement locText(String[] x, String v) {
    return doLocate("RedRobot.isText", v, x);
  }

  private WebElement locKey(String... x) {
    return doLocate("RedRobot.isKey", null, x);
  }

  private WebElement locCheckable(String... x) {
    return doLocate("RedRobot.isCheckable", null, x);
  }

  @Override
  public void open(URL url) {
     webDriver.get(url.toString());
     waitTillReady();
  }

  private String[] allButLast(String[] x) {
    String[] n = new String[x.length - 1];
    for (int i = 0; i < n.length; i++) {
      n[i] = x[i];
    }
    return n;
  }

  @Override
  public void set(String... x) {
    String[] n = allButLast(x);
    String v = x[x.length - 1];
    WebElement e = this.locKey(n);
    try {
      if ("select".equals(e.getTagName())) {
        e.sendKeys(v);
      } else {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("if (arguments[0].value != arguments[1]) {arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change'))}", e, v);
      }
    } catch (WebDriverException ex) {
      throw new RuntimeException("Failed trying to set name='"
          + e.getTagName() + "' to text='" + e.getText() + "'", ex);
    }
    waitTillReady();
  }

  @Override
  public void close() {
    webDriver.quit();
  }

  @Override
  public void setReadyStrategy(ReadyStrategy p) {
    this.readyStrategy = p;
  }

  @Override
  public void addListener(RobotListener listener) {
    listeners.add(listener);
  }

  @Override
  public <T> T unwrap(Class<T> implClass) {
      return (T) webDriver;
  }
}
