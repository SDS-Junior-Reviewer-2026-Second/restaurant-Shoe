package com.sds.cleancode.restaurant;

public class TestableMailSender extends MailSender {

    private int countSendMailMethodIsCalled;

    @Override
    public void sendMail(Schedule schedule) {
        countSendMailMethodIsCalled++;
    }

    public int getCountSendMailMethodIsCalled() {
        return countSendMailMethodIsCalled;
    }
}