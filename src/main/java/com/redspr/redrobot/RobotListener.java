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

// EXPERIMENTAL
public interface RobotListener {
  /**
   * Called before the wait strategy is called.
   */
  void waitTillReadyStart();

  /**
   * Called after the wait strategy is called.
   */
  void waitTillReadyEnd();

  /**
   * Called before each action is executed. e.g. before click
   */
  // XXX say which action?
  void actionStart();

  /**
   * Called after each action is executed. e.g. after set
   */
  void actionEnd();

  /**
   * Called before a locator is evaluated.
   */
  void locatorStart();

  /**
   * Called after a locator is evaluated.
   *
   * @param result - the result of the locator execution.
   */
  void locatorEnd(LocatorResult result);
}
