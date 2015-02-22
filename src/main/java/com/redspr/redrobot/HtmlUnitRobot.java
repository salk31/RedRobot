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

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.redspr.redrobot.HtmlUnitRobotWorker.Command;

public class HtmlUnitRobot implements Robot {

  private static final Logger LOGGER =
            Logger.getLogger(HtmlUnitRobot.class.getName());

  private final HtmlUnitRobotWorker worker;

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

  public HtmlUnitRobot() {
    this.worker = new HtmlUnitRobotWorker();
    (new Thread(worker)).start();

    URL url = getClass().getResource("/redRobotCore.js");

    try {
      SCRIPT = Resources.toString(url, Charsets.UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException("Unable to read script" , ex);
    }
  }

  private void call(Command foo) {
    worker.queue(foo);
  }

  @Override
  public void back() {
    call(new Command() {
      @Override
      public void execute(HtmlUnitRobotWorker webDriver) throws Exception {
        webDriver.getWebWindow().getHistory().back();
      }
    });
    waitTillReady();
  }

  @Override
  public void forward() {
    call(new Command() {
      @Override
      public void execute(HtmlUnitRobotWorker webDriver) throws Exception {
        webDriver.getWebWindow().getHistory().forward();
      }
    });

    waitTillReady();
  }

  @Override
  public void reload() {
    call(new Command() {
      @Override
      public void execute(HtmlUnitRobotWorker webDriver) throws Exception {
        webDriver.getPage().refresh();
      }
    });
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
      Double rawResult = (Double) engine.eval("RedRobot.multiTextMatch(source, input)");
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
    HtmlUnitRobotWorker.Alert alert = worker.getAlert();
    if (alert != null) {

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

    } else {
      // fine, was no alert
      HtmlElement elmt = locClickable(x);
      for (RobotListener l : listeners) {
        l.actionStart();
      }
      try {
        elmt.click();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      for (RobotListener l : listeners) {
        l.actionEnd();
      }
      waitTillReady();
    }
  }

  /**
   * Use the provided wait strategy and call listeners.
   */
  private void waitTillReady() {
    while (!worker.isIdle() && worker.getAlert() == null) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

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
    throw new RuntimeException("Not implemented");
  }

  @Override
  public boolean textExists(String... x) {
    HtmlUnitRobotWorker.Alert alert = worker.getAlert();
    if (alert != null) {
      return isMatch(new String[]{alert.getText(), OK, CANCEL}, x) > 0;
    } else {
      String[] n = allButLast(x);
      String v = x[x.length - 1];
      try {
        locText(n , v);
      } catch (NotFoundException ex2) {
        return false;
      }
      return true;
    }
  }

  @Override
  public String get(String... x) {
    HtmlElement elmt = locKey(x);
    String r = elmt.getAttribute("value");

    if (r == null || "".equals(r)) {
        r = elmt.asText();
    }
    return r.replaceAll("\\r", "");
  }

  @Override
  public String getConfirmation() {
    throw new RuntimeException("Not supported");
  }

  @Override
  public boolean isSelected(String... x) {
    HtmlElement e = locCheckable(x);
    if (e instanceof HtmlCheckBoxInput) {
        return ((HtmlCheckBoxInput) e).isChecked();
    }
    if (e instanceof HtmlRadioButtonInput) {
        return ((HtmlRadioButtonInput) e).isChecked();
    }
    throw new RuntimeException("Don't know how to handle " + e);
  }

  @Override
  public boolean isChecked(String... x) {
    return isSelected(x);
  }

  private HtmlElement doLocate(final String cmd, final Object cmdArg,
      final String[] args) {
    // TODO __ need to check worker is idle?
    Gson gson = new Gson();
    ScriptResult rawResult2 = worker.getPage().executeJavaScript(
        SCRIPT
        + ";RedRobot.findBestMatches(document, " + cmd + " , "
            + gson.toJson(cmdArg) + ", " + gson.toJson(args) + ")\n;"

    );
    Object rawResult = rawResult2.getJavaScriptResult();

    if (!(rawResult instanceof List)) {
      throw new RuntimeException("Expected a list but got '" + rawResult + "'");
    }

    for (RobotListener l : listeners) {
      l.locatorStart();
    }

    List<HTMLElement> y = (List) rawResult;
    for (HTMLElement we : y) {
      try {
        HtmlElement dom = we.getDomNodeOrDie();
        if (dom.isDisplayed()) {
          for (RobotListener l : listeners) {
            l.locatorEnd(new LocatorResultImpl(we));
          }

          return dom;
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

  private HtmlElement locClickable(String... x) {
    return doLocate("RedRobot.isClickable", null, x);
  }

  private HtmlElement locText(String[] x, String v) {
    return doLocate("RedRobot.isText", v, x);
  }

  private HtmlElement locKey(String... x) {
    return doLocate("RedRobot.isKey", null, x);
  }

  private HtmlElement locCheckable(String... x) {
    return doLocate("RedRobot.isCheckable", null, x);
  }

  @Override
  public void open(final URL url) {
    call(new Command() {
      @Override
      public void execute(HtmlUnitRobotWorker webDriver) throws Exception {
        webDriver.getWebClient().getPage(url);
      }
    });

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
    HtmlElement e = this.locKey(n);
   // try {
        if (e instanceof HtmlTextArea) {
            ((HtmlTextArea) e).setText(v);
        } else if (e instanceof HtmlTextInput) {
            ((HtmlTextInput) e).setText(v);
        }
        //e.setNodeValue("");
        //e.type(v);
    //} catch (IOException ex) {
    //        throw new RuntimeException("Failed trying to click on name='"
    //                + e.getTagName() + "' text='" + e + "'", ex);
    //    }
    waitTillReady();
  }

  @Override
  public void close() {
    worker.stop();
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
    return (T) worker.getWebClient();
  }
}
