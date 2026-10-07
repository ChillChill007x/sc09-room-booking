package com.example.cp_room_booking.event;

import com.example.cp_room_booking.domain.enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * publish เมื่อสร้างการจองใหม่ โมดูลการจองไม่ต้องรู้ว่าใครรับ event นี้ (Observer)
 */
public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        String roomCode,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status
) {
}
