package com.redspr.redrobot;

/**
 * @author sam@redspr.com
 */
public class SleepReadyStrategy implements ReadyStrategy {

  @Override
  public void waitTillReady() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // just fall through
    }
  }
}
