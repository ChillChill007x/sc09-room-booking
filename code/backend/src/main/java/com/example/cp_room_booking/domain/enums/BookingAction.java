package com.example.cp_room_booking.domain.enums;

/**
 * การกระทำที่เปลี่ยนสถานะการจองได้
 */
public enum BookingAction {
    APPROVE,
    REJECT,
    CANCEL,
    CHECK_IN,
    COMPLETE,
    MARK_NO_SHOW
}
