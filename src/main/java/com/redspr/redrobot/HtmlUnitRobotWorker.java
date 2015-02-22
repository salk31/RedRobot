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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlUnitRobotWorker implements Runnable  {

  public interface Command {
    void execute(HtmlUnitRobotWorker webClient) throws Exception;
  }

  public class Alert {

    private final String message;

    private final LinkedBlockingQueue<Boolean> answer = new LinkedBlockingQueue<Boolean>();

    Alert(String message) {
        this.message = message;
    }

    public String getText() {
       return message;
    }

    public void accept() {
        alert = null;
        answer.add(Boolean.TRUE);
    }

    public void dismiss() {
        alert = null;
        answer.add(Boolean.FALSE);
    }

    public boolean poll() {
        try {
          return Boolean.TRUE.equals(answer.poll(100, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
    }
  }



  private static final Logger LOGGER =
            Logger.getLogger(HtmlUnitRobotWorker.class.getName());

  /**
   * Wrapped implementation.
   */
  private final WebClient webDriver;

  private LinkedBlockingQueue<Command> todo = new LinkedBlockingQueue<Command>();

  private Alert alert;

  public HtmlUnitRobotWorker() {
    this.webDriver = new WebClient();

    webDriver.setConfirmHandler(new ConfirmHandler() {
      @Override
      public boolean handleConfirm(Page page, String message) {
        alert = new Alert(message);
        return alert.poll();
      }
    });

    webDriver.setAlertHandler(new AlertHandler() {
      @Override
      public void handleAlert(Page page, String message) {
        alert = new Alert(message);
        alert.poll();
      }
    });
  }

  public HtmlUnitRobotWorker.Alert getAlert() {
    return alert;
  }

  public void queue(Command foo) {
    try {
      todo.put(foo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public WebClient getWebClient() {
    return webDriver;
  }

  public WebWindow getWebWindow() {
    return webDriver.getWebWindows().get(0);
  }

  public HtmlPage getPage() {
    return (HtmlPage) getWebWindow().getEnclosedPage();
  }

  private final Command marrakech = new Command() {

    @Override
    public void execute(HtmlUnitRobotWorker webClient) throws Exception {

    }

  };

  @Override
  public void run() {
    try {
      while (todo != null) {
        Command foo = todo.poll(100, TimeUnit.SECONDS);
        if (foo != marrakech) {
          todo.add(marrakech);
        }
        foo.execute(this);
      }
    } catch (Exception ex) {
      // TODO __
      ex.printStackTrace();
    }
  }

  public boolean isIdle() {
    return todo.isEmpty();
  }

  public void stop() {
    todo = null;
  }
}
