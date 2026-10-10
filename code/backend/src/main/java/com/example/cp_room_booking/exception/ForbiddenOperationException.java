package com.example.cp_room_booking.exception;

/**
 * ผู้ใช้ไม่มีสิทธิ์ทำรายการกับข้อมูลนี้ ตอบ 403
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
