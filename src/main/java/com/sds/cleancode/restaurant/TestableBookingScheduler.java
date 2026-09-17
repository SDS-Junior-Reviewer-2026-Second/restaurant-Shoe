package com.sds.cleancode.restaurant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestableBookingScheduler extends BookingScheduler {

    private String dateTime;

    public TestableBookingScheduler(
            int capacityPerHour,
            String dateTime
    ) {
        super(capacityPerHour);
        this.dateTime = dateTime;
    }

    @Override
    public LocalDateTime getNow() {
        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        return LocalDateTime.parse(dateTime, format);
    }
}