package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.util.Map;

@Builder
public record StatsSummaryResponse(
        long totalBookings,
        long todayBookings,
        long pendingApprovals,
        long totalRooms,
        long totalUsers,
        Map<BookingStatus, Long> bookingsByStatus
) {
}
