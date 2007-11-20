package com.redspr.redrobot;


import java.io.IOException;

public abstract class Robot {

abstract public void open(String path);

abstract public String getConfirmation();
    abstract public void back() throws IOException;
    
    abstract public void click(String... x) throws IOException;

    abstract public int findText(String x);

    abstract public String get(String... x);

   // abstract protected DomNode getEntry(String[] x);

    abstract public boolean isChecked(String... x);

    abstract public void selenium(String x);

    abstract public void set(String... x);
}
