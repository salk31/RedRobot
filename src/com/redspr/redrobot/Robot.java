package com.redspr.redrobot;

public interface Robot {

    void back();

    void click(String... x);

    int findText(String x);

    String get(String... x);

    String getConfirmation();

    boolean isChecked(String... x);

    void open(String path);

    void set(String... x);
}
