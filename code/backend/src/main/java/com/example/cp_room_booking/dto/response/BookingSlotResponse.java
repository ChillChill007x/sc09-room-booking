package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * ช่วงเวลาที่ถูกจองในตารางห้องรายวัน ไม่เปิดเผยข้อมูลผู้จอง
 */
@Builder
public record BookingSlotResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status
) {
}
