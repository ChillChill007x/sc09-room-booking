package com.example.cp_room_booking.domain.enums;

/**
 * ACTIVE เปิดให้จอง, MAINTENANCE ปิดซ่อมชั่วคราว, INACTIVE เลิกใช้แล้ว (ลบแบบ soft delete)
 */
public enum RoomStatus {
    ACTIVE,
    MAINTENANCE,
    INACTIVE
}
