package com.redspr.redrobot;

import java.net.URL;

public interface Robot {

    void back();

    void click(String... x);

    int findText(String x);

    String get(String... x);

    String getConfirmation();

    boolean isChecked(String... x);

    void open(URL url);

    void set(String... x);
    
    // TODO 04 a dump/debug method?
}
