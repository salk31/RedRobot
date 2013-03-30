package com.redspr.redrobot;

import java.util.concurrent.LinkedBlockingQueue;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Beta;
import org.openqa.selenium.security.Credentials;

class Bar implements Alert {

    private final String msg;

    public LinkedBlockingQueue semaphore = new LinkedBlockingQueue();

    Bar(String msg2) {
        this.msg = msg2;
    }

    @Override
    public void dismiss() {
        semaphore.add(1);
    }

    @Override
    public void accept() {
        semaphore.add(1);
    }

    @Override
    public String getText() {
        return msg;
    }

    @Override
    public void sendKeys(String keysToSend) {
        // TODO Auto-generated method stub

        throw new RuntimeException(
                "not Alert.sendKeys implemented");

    }

    @Override
    @Beta
    public void authenticateUsing(
            Credentials credentials) {
        // TODO Auto-generated method stub

        throw new RuntimeException(
                "not Alert.authenticateUsing implemented");

    }

};