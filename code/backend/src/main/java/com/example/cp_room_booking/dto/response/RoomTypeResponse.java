package com.example.cp_room_booking.dto.response;

import lombok.Builder;

@Builder
public record RoomTypeResponse(Long id, String name, String description) {
}
