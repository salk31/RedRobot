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

import java.net.URL;

public interface Robot {

  void back();

  void click(String... x);

  /**
   * Count the visible text matching x on this page.
   *
   * NB This method is inconsistent with other locator methods and is being phased out.
   *
   * @param x - substring to be searched for
   * @return number of elements found
   */
  @Deprecated
  int findText(String x);

  /**
   * EXPERIMENTAL - More robust method to check if text on a page.
   *
   * @param x - the list of locator strings to be used
   * @return true iff the locator text is present on the page and visible.
   */
  // TODO 00 multiple values best for disambiguation but doesn't filter?
  // so useless in this context? just does an AND?
  boolean textExists(String... x);

  String get(String... x);

  @Deprecated // no replacement, use click(message, "OK")...
  String getConfirmation();

  @Deprecated // use isSelected
  boolean isChecked(String... x);

  boolean isSelected(String... x);

  void open(URL url);

  void set(String... x);

  void close();

  // TODO 04 a dump/debug method?
}
