package com.sds.cleancode.restaurant;

public class TestableSmsSender extends SmsSender {

    private boolean sendMethodIsCalled;

    @Override
    public void send(Schedule schedule) {
        sendMethodIsCalled = true;
    }

    public boolean isSendMethodIsCalled() {
        return sendMethodIsCalled;
    }
}