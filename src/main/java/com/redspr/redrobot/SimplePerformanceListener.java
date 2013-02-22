package com.redspr.redrobot;

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
