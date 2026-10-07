package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.domain.enums.RoomStatus;

/**
 * ตัวกรองของ GET /api/v1/rooms ทุกช่องเว้นว่างได้
 */
public record RoomSearchRequest(
        Integer floor,
        Long typeId,
        Integer minCapacity,
        Long equipmentId,
        String keyword,
        RoomStatus status
) {
}
