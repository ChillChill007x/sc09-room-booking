package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * ช่วงเวลาที่ถูกจองในตารางรวมทุกห้องรายวัน ไม่เปิดเผยข้อมูลผู้จองและวัตถุประสงค์
 */
@Builder
public record ScheduleSlotResponse(
        Long bookingId,
        Long roomId,
        String roomCode,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status
) {
}
