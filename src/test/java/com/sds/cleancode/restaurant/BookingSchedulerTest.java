package com.sds.cleancode.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static final LocalDateTime ON_THE_HOUR =
            LocalDateTime.parse("2021/03/26 09:00", FORMAT);

    private static final LocalDateTime NOT_ON_THE_HOUR =
            LocalDateTime.parse("2021/03/26 09:05", FORMAT);

    private static final int UNDER_CAPACITY = 1;
    private static final int CAPACITY_PER_HOUR = 3;

    @Mock
    private Customer CUSTOMER;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private Customer CUSTOMER_WITH_MAIL;

    @Mock
    private SmsSender smsSender;

    @Mock
    private MailSender mailSender;

    @Spy
    private BookingScheduler bookingScheduler =
            new BookingScheduler(CAPACITY_PER_HOUR);

    @BeforeEach
    public void setUp() {
        bookingScheduler.setMailSender(mailSender);
        bookingScheduler.setSmsSender(smsSender);
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

        verify(smsSender, times(1)).send(schedule);
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {

        Schedule schedule =
                new Schedule(ON_THE_HOUR, UNDER_CAPACITY, CUSTOMER);

        bookingScheduler.addSchedule(schedule);

        verify(mailSender, times(0)).sendMail(schedule);
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {

        Schedule schedule =
                new Schedule(
                        ON_THE_HOUR,
                        UNDER_CAPACITY,
                        CUSTOMER_WITH_MAIL
                );

        bookingScheduler.addSchedule(schedule);

        verify(mailSender, times(1)).sendMail(schedule);
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {

        LocalDateTime sunday =
                LocalDateTime.parse("2021/03/28 17:00", FORMAT);

        when(bookingScheduler.getNow()).thenReturn(sunday);

        Schedule newSchedule =
                new Schedule(
                        ON_THE_HOUR,
                        UNDER_CAPACITY,
                        CUSTOMER_WITH_MAIL
                );

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

        LocalDateTime monday =
                LocalDateTime.parse("2024/06/03 17:00", FORMAT);

        when(bookingScheduler.getNow()).thenReturn(monday);

        Schedule newSchedule =
                new Schedule(
                        ON_THE_HOUR,
                        UNDER_CAPACITY,
                        CUSTOMER_WITH_MAIL
                );

        bookingScheduler.addSchedule(newSchedule);

        assertTrue(
                bookingScheduler.hasSchedule(newSchedule)
        );
    }
}