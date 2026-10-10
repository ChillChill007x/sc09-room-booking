package com.example.cp_room_booking.dto.response;

import lombok.Builder;

@Builder
public record EquipmentResponse(Long id, String name, String description) {
}
