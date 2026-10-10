package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.response.CalendarDayResponse;
import com.example.cp_room_booking.dto.response.ScheduleSlotResponse;
import com.example.cp_room_booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    private static final LocalDate DAY = LocalDate.of(2030, 1, 15);

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private final Room lab = Room.builder().id(2L).code("SC09-9226").build();
    private final Room meeting = Room.builder().id(6L).code("SC09-9231").build();

    @Test
    void monthSummary_countsBookingsPerDayInDateOrder() {
        YearMonth month = YearMonth.of(2030, 1);
        when(bookingRepository.findAllInRange(LocalDateTime.of(2030, 1, 1, 0, 0), LocalDateTime.of(2030, 2, 1, 0, 0),
                ScheduleServiceImpl.SHOWN)).thenReturn(List.of(
                booking(1L, lab, DAY.plusDays(2).atTime(9, 0)),
                booking(2L, lab, DAY.atTime(9, 0)),
                booking(3L, meeting, DAY.atTime(13, 0))));

        List<CalendarDayResponse> days = scheduleService.monthSummary(month);

        assertThat(days).extracting(CalendarDayResponse::date, CalendarDayResponse::bookings)
                .containsExactly(tuple(DAY, 2L), tuple(DAY.plusDays(2), 1L));
    }

    @Test
    void daySchedule_returnsSlotsOfAllRoomsWithoutBookerData() {
        when(bookingRepository.findAllInRange(DAY.atStartOfDay(), DAY.plusDays(1).atStartOfDay(),
                ScheduleServiceImpl.SHOWN)).thenReturn(List.of(booking(2L, lab, DAY.atTime(9, 0)),
                booking(3L, meeting, DAY.atTime(13, 0))));

        List<ScheduleSlotResponse> slots = scheduleService.daySchedule(DAY);

        assertThat(slots).extracting(ScheduleSlotResponse::bookingId, ScheduleSlotResponse::roomId,
                        ScheduleSlotResponse::roomCode, ScheduleSlotResponse::startTime)
                .containsExactly(tuple(2L, 2L, "SC09-9226", DAY.atTime(9, 0)),
                        tuple(3L, 6L, "SC09-9231", DAY.atTime(13, 0)));
    }

    @Test
    void shownStatuses_excludeRejectedCancelledAndNoShow() {
        scheduleService.daySchedule(DAY);

        verify(bookingRepository).findAllInRange(eq(DAY.atStartOfDay()), eq(DAY.plusDays(1).atStartOfDay()),
                eq(ScheduleServiceImpl.SHOWN));
        assertThat(ScheduleServiceImpl.SHOWN).containsExactlyInAnyOrder(BookingStatus.PENDING,
                BookingStatus.APPROVED, BookingStatus.CHECKED_IN, BookingStatus.COMPLETED);
    }

    private Booking booking(Long id, Room room, LocalDateTime start) {
        return Booking.builder().id(id).room(room).startTime(start).endTime(start.plusHours(2))
                .status(BookingStatus.APPROVED).build();
    }
}
