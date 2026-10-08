package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.util.List;

/**
 * action ที่ผู้ใช้คนนี้ทำกับการจองนี้ได้ตอนนี้ frontend ใช้ตัดสินว่าจะแสดงปุ่มไหน
 */
@Builder
public record AllowedActionsResponse(
        Long bookingId,
        BookingStatus status,
        List<BookingAction> actions
) {
}
