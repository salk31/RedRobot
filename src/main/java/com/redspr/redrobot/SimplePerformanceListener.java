package com.redspr.redrobot;

public class SimplePerformanceListener implements RobotListener {
  private long t0;

  private long waitTillReadyTotal;

  private long actionTotal;

  @Override
  public void waitTillReadyStart() {
    t0 = System.currentTimeMillis();
  }

  @Override
  public void waitTillReadyEnd() {
    long t = System.currentTimeMillis() - t0;
    waitTillReadyTotal += t;
  }

  @Override
  public void actionStart() {
    t0 = System.currentTimeMillis();
  }

  @Override
  public void actionEnd() {
    long t = System.currentTimeMillis() - t0;
    actionTotal += t;
  }

  @Override
  public void locatorStart() {
  }

  @Override
  public void locatorEnd(LocatorResult result) {
  }

  public void reset() {
    waitTillReadyTotal = 0;
    actionTotal = 0;
  }

  public long getTotal() {
    return actionTotal + waitTillReadyTotal;
  }

}
