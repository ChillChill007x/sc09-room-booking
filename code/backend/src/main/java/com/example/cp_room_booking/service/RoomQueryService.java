package com.example.cp_room_booking.service;

import com.example.cp_room_booking.domain.entity.Room;

import java.time.LocalDateTime;

/**
 * ข้อมูลห้องที่โมดูลการจอง (คนที่ 3) ต้องใช้ แยกจาก RoomService ตาม Interface Segregation
 * โมดูลการจองจึงไม่ต้องพึ่ง method CRUD ของห้องที่ไม่ได้ใช้
 */
public interface RoomQueryService {

    /**
     * คืนห้องที่เปิดให้จอง ไม่พบตอบ 404 ห้องไม่ ACTIVE ตอบ 400
     */
    Room getActiveRoom(Long roomId);

    boolean isClosed(Long roomId, LocalDateTime start, LocalDateTime end);

    boolean existsById(Long roomId);
}
