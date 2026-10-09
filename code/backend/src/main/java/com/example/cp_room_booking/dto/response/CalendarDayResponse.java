package com.example.cp_room_booking.dto.response;

import lombok.Builder;

import java.time.LocalDate;

/**
 * จำนวนการจองในแต่ละวัน ใช้ทำเครื่องหมายบนปฏิทินรายเดือน
 */
@Builder
public record CalendarDayResponse(
        LocalDate date,
        long bookings
) {
}
