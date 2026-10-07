package com.example.cp_room_booking.event;

import com.example.cp_room_booking.domain.enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * publish ทุกครั้งที่สถานะการจองเปลี่ยน userId คือเจ้าของการจอง
 */
public record BookingStatusChangedEvent(
        Long bookingId,
        Long userId,
        BookingStatus fromStatus,
        BookingStatus toStatus,
        String note,
        String roomCode,
        LocalDateTime startTime
) {
}
