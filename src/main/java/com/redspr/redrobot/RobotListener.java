package com.redspr.redrobot;

// EXPERIMENTAL
public interface RobotListener {
  void waitTillReadyStart();
  void waitTillReadyEnd();

  // XXX say which action?
  void actionStart();
  void actionEnd();

  void locatorStart();
  void locatorEnd(LocatorResult result);
}
