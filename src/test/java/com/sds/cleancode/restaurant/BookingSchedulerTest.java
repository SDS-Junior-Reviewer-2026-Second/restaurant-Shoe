package com.sds.cleancode.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingSchedulerTest {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static final LocalDateTime ON_THE_HOUR =
            LocalDateTime.parse("2021/03/26 09:00", FORMAT);

    private static final LocalDateTime NOT_ON_THE_HOUR =
            LocalDateTime.parse("2021/03/26 09:05", FORMAT);

    private static final Customer CUSTOMER =
            new Customer("Fake name", "010-1234-5678");

    private static final int UNDER_CAPACITY = 1;
    private static final int CAPACITY_PER_HOUR = 3;


    private BookingScheduler bookingScheduler;
    private TestableSmsSender testableSmsSender;
    private TestableMailSender testableMailSender;


    @BeforeEach
    public void setUp() {
        bookingScheduler =
                new BookingScheduler(CAPACITY_PER_HOUR);

        testableSmsSender =
                new TestableSmsSender();

        testableMailSender =
                new TestableMailSender();

        bookingScheduler.setMailSender(testableMailSender);
        bookingScheduler.setSmsSender(testableSmsSender);
    }

    @Test
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {

        Schedule schedule =
                new Schedule(NOT_ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        assertThrows(RuntimeException.class, () -> {
            bookingScheduler.addSchedule(schedule);
        });
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        assertTrue(bookingScheduler.hasSchedule(schedule));
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대에_Capacity_초과할_경우_예외발생() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        Schedule newSchedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> {
                    bookingScheduler.addSchedule(newSchedule);
                });

        assertEquals(
                "Number of people is over restaurant capacity per hour",
                exception.getMessage()
        );
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대가_다르면_Capacity_차있어도_스케쥴_추가_성공() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, CAPACITY_PER_HOUR, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        LocalDateTime differentHour =
                ON_THE_HOUR.plusHours(1);

        Schedule newSchedule =
                new Schedule(differentHour, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(newSchedule);

        assertTrue(bookingScheduler.hasSchedule(newSchedule));
    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        assertTrue(testableSmsSender.isSendMethodIsCalled());
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        assertEquals(
                0,
                testableMailSender.getCountSendMailMethodIsCalled()
        );
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {

        Customer customerWithMail =
                new Customer(
                        "Fake Name",
                        "010-1234-5678",
                        "test@test.com"
                );

        Schedule schedule =
                new Schedule(
                        ON_THE_HOUR,
                        UNDER_CAPACITY,
                        customerWithMail
                );

        bookingScheduler.addSchedule(schedule);

        assertEquals(
                1,
                testableMailSender.getCountSendMailMethodIsCalled()
        );
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {

        bookingScheduler =
                new TestableBookingScheduler(
                        CAPACITY_PER_HOUR,
                        "2021/03/28 17:00"
                );

        Schedule newSchedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> {
                    bookingScheduler.addSchedule(newSchedule);
                });

        assertEquals(
                "Booking system is not available on sunday",
                exception.getMessage()
        );
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {

        bookingScheduler =
                new TestableBookingScheduler(
                        CAPACITY_PER_HOUR,
                        "2021/03/29 17:00"
                );

        Schedule newSchedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(newSchedule);

        assertTrue(
                bookingScheduler.hasSchedule(newSchedule)
        );
    }
}