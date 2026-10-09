package com.example.cp_room_booking.service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * ข้อมูลการจองที่โมดูลห้อง (คนที่ 2) ต้องใช้ แยกเป็น interface เล็กตาม Interface Segregation
 * โมดูลห้องจึงไม่ต้องรู้จัก BookingRepository
 */
public interface BookingQueryService {

    Set<Long> findBookedRoomIds(LocalDateTime start, LocalDateTime end);

    boolean hasFutureBookings(Long roomId);

    /**
     * มีการจองที่ยังใช้งาน (รออนุมัติ อนุมัติแล้ว หรือกำลังใช้ห้อง) ทับช่วงเวลานี้หรือไม่ ใช้ตอนสร้างช่วงปิดห้อง
     */
    boolean hasActiveBookingOverlap(Long roomId, LocalDateTime start, LocalDateTime end);
}
