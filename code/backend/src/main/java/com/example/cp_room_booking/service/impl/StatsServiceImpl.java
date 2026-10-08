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
import com.example.cp_room_booking.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    /**
     * นับเป็นการใช้ห้องจริงเฉพาะการจองที่อนุมัติแล้ว กำลังใช้ หรือใช้เสร็จแล้ว
     */
    private static final Set<BookingStatus> USAGE_STATUSES =
            Set.of(BookingStatus.APPROVED, BookingStatus.CHECKED_IN, BookingStatus.COMPLETED);
    private static final long MAX_RANGE_DAYS = 366;

    private final BookingStatsRepository bookingStatsRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public StatsSummaryResponse summary() {
        Map<BookingStatus, Long> byStatus = new EnumMap<>(BookingStatus.class);
        for (BookingStatus status : BookingStatus.values()) {
            byStatus.put(status, 0L);
        }
        bookingStatsRepository.countByStatus().forEach(row -> byStatus.put(row.getStatus(), row.getTotal()));

        LocalDate today = LocalDate.now(clock);
        return StatsSummaryResponse.builder()
                .totalBookings(byStatus.values().stream().mapToLong(Long::longValue).sum())
                .todayBookings(bookingStatsRepository.countStartingBetween(today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay()))
                .pendingApprovals(byStatus.get(BookingStatus.PENDING))
                .totalRooms(roomRepository.count())
                .totalUsers(userRepository.count())
                .bookingsByStatus(byStatus)
                .build();
    }

    /**
     * ชั่วโมงใช้งานต่อห้องในช่วงวันที่ from ถึง to (รวมทั้งสองวัน)
     * การจองที่คร่อมขอบช่วงจะนับเฉพาะส่วนที่อยู่ในช่วง
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomUsageResponse> roomUsage(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException("วันที่เริ่มต้องไม่หลังวันที่สิ้นสุด");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BusinessRuleException("เลือกช่วงได้ไม่เกิน " + MAX_RANGE_DAYS + " วัน");
        }
        LocalDateTime rangeStart = from.atStartOfDay();
        LocalDateTime rangeEnd = to.plusDays(1).atStartOfDay();

        Map<Room, List<Booking>> byRoom = bookingStatsRepository.findUsage(rangeStart, rangeEnd, USAGE_STATUSES)
                .stream()
                .collect(Collectors.groupingBy(Booking::getRoom));

        return byRoom.entrySet().stream()
                .map(entry -> RoomUsageResponse.builder()
                        .roomId(entry.getKey().getId())
                        .roomCode(entry.getKey().getCode())
                        .roomName(entry.getKey().getName())
                        .bookingCount(entry.getValue().size())
                        .totalHours(totalHours(entry.getValue(), rangeStart, rangeEnd))
                        .build())
                .sorted(Comparator.comparingDouble(RoomUsageResponse::totalHours).reversed()
                        .thenComparing(RoomUsageResponse::roomCode))
                .toList();
    }

    private double totalHours(List<Booking> bookings, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        long minutes = bookings.stream()
                .mapToLong(booking -> {
                    LocalDateTime start = booking.getStartTime().isBefore(rangeStart) ? rangeStart : booking.getStartTime();
                    LocalDateTime end = booking.getEndTime().isAfter(rangeEnd) ? rangeEnd : booking.getEndTime();
                    return Duration.between(start, end).toMinutes();
                })
                .sum();
        return Math.round(minutes / 60.0 * 100) / 100.0;
    }
}
