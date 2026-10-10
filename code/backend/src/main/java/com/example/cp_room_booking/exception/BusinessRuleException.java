package com.example.cp_room_booking.exception;

/**
 * คำขอผิดกฎทางธุรกิจ ตอบ 400
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
