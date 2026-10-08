package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.response.RoomUsageResponse;
import com.example.cp_room_booking.dto.response.StatsSummaryResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.repository.BookingStatsRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");
    private static final LocalDate FROM = LocalDate.of(2030, 1, 1);
    private static final LocalDate TO = LocalDate.of(2030, 1, 31);

    @Mock
    private BookingStatsRepository bookingStatsRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;

    private StatsServiceImpl statsService;
    private final Room lab = Room.builder().id(1L).code("SC09-2201").name("Lab").build();
    private final Room meeting = Room.builder().id(2L).code("SC09-3303").name("ประชุม").build();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(LocalDateTime.of(2030, 1, 15, 8, 0).atZone(ZONE).toInstant(), ZONE);
        statsService = new StatsServiceImpl(bookingStatsRepository, roomRepository, userRepository, clock);
    }

    @Test
    void summary_fillsMissingStatusesWithZeroAndSumsTotal() {
        when(bookingStatsRepository.countByStatus()).thenReturn(List.of(
                count(BookingStatus.PENDING, 3), count(BookingStatus.APPROVED, 5)));
        when(bookingStatsRepository.countStartingBetween(any(), any())).thenReturn(2L);
        when(roomRepository.count()).thenReturn(12L);
        when(userRepository.count()).thenReturn(5L);

        StatsSummaryResponse summary = statsService.summary();

        assertThat(summary.totalBookings()).isEqualTo(8);
        assertThat(summary.pendingApprovals()).isEqualTo(3);
        assertThat(summary.todayBookings()).isEqualTo(2);
        assertThat(summary.bookingsByStatus()).hasSize(7).containsEntry(BookingStatus.NO_SHOW, 0L);
    }

    @Test
    void roomUsage_sumsHoursPerRoomSortedByHours() {
        when(bookingStatsRepository.findUsage(any(), any(), any())).thenReturn(List.of(
                booking(lab, "2030-01-10T09:00", "2030-01-10T11:00"),
                booking(lab, "2030-01-11T13:00", "2030-01-11T14:30"),
                booking(meeting, "2030-01-12T10:00", "2030-01-12T11:00")));

        List<RoomUsageResponse> usage = statsService.roomUsage(FROM, TO);

        assertThat(usage).extracting(RoomUsageResponse::roomCode).containsExactly("SC09-2201", "SC09-3303");
        assertThat(usage.get(0).totalHours()).isEqualTo(3.5);
        assertThat(usage.get(0).bookingCount()).isEqualTo(2);
        assertThat(usage.get(1).totalHours()).isEqualTo(1.0);
    }

    @Test
    void roomUsage_clipsBookingCrossingRangeBoundary() {
        when(bookingStatsRepository.findUsage(any(), any(), any())).thenReturn(List.of(
                booking(lab, "2029-12-31T23:00", "2030-01-01T02:00")));

        assertThat(statsService.roomUsage(FROM, TO).get(0).totalHours()).isEqualTo(2.0);
    }

    @Test
    void roomUsage_fromAfterTo_throwsBusinessRule() {
        assertThatThrownBy(() -> statsService.roomUsage(TO, FROM)).isInstanceOf(BusinessRuleException.class);
    }

    private Booking booking(Room room, String start, String end) {
        return Booking.builder().room(room).startTime(LocalDateTime.parse(start)).endTime(LocalDateTime.parse(end))
                .status(BookingStatus.COMPLETED).build();
    }

    private BookingStatsRepository.StatusCount count(BookingStatus status, long total) {
        return new BookingStatsRepository.StatusCount() {
            @Override
            public BookingStatus getStatus() {
                return status;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
