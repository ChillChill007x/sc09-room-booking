package com.example.cp_room_booking.exception;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;

/**
 * สถานะปัจจุบันทำ action นี้ไม่ได้ ตอบ 409
 */
public class InvalidBookingStateException extends ConflictException {

    public InvalidBookingStateException(BookingStatus status, BookingAction action) {
        super("การจองสถานะ " + status + " ทำรายการ " + action + " ไม่ได้");
    }
}
