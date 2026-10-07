package com.example.cp_room_booking.dto.response;

import lombok.Builder;

@Builder
public record RoomEquipmentResponse(Long equipmentId, String name, int quantity) {
}
