package com.example.cp_room_booking.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RoomClosureResponse(
        Long id,
        Long roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        LocalDateTime createdAt
) {
}
