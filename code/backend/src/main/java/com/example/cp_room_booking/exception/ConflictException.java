package com.example.cp_room_booking.exception;

/**
 * ข้อมูลชนกันหรือสถานะไม่อนุญาตให้ทำรายการ ตอบ 409
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
