package com.redspr.redrobot;

import java.util.List;

import org.openqa.selenium.WebElement;

public class WebDriverResult implements Result {

    WebDriverRobot driver;

    List<WebElement> hits;

    WebDriverResult(WebDriverRobot driver2, List<WebElement> hits2) {
        this.driver = driver2;
        this.hits = hits2;
    }

    @Override
    public void renderDebug() {
        driver.debug(hits);
    }

    @Override
    public void clearDebug() {
        driver.clearDebug();
    }

}
