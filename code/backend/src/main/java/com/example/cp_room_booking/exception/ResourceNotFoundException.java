package com.example.cp_room_booking.exception;

/**
 * ไม่พบข้อมูลที่ร้องขอ ตอบ 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("ไม่พบ" + resource + " id=" + id);
    }
}
