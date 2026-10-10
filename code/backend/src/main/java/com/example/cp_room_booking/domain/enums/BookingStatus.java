package com.example.cp_room_booking.domain.enums;

import java.util.Set;

/**
 * สถานะการจอง 7 สถานะ ใช้ร่วมกันระหว่างโมดูลการจอง (คนที่ 3) และวงจรสถานะ (คนที่ 4)
 */
public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    CHECKED_IN,
    COMPLETED,
    NO_SHOW;

    /**
     * สถานะที่ยังจองห้องอยู่จริง ใช้ตรวจเวลาชนและนับจำนวนการจองที่ค้างอยู่
     */
    public static final Set<BookingStatus> ACTIVE = Set.of(PENDING, APPROVED, CHECKED_IN);
}
