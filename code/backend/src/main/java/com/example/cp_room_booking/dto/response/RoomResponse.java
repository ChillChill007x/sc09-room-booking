package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.RoomStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomResponse(
        Long id,
        String code,
        String name,
        Integer floor,
        Integer capacity,
        String description,
        RoomStatus status,
        RoomTypeResponse roomType,
        List<RoomEquipmentResponse> equipment
) {
}
