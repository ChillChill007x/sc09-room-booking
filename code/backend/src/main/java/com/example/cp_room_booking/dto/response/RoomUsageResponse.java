package com.example.cp_room_booking.dto.response;

import lombok.Builder;

@Builder
public record RoomUsageResponse(
        Long roomId,
        String roomCode,
        String roomName,
        long bookingCount,
        double totalHours
) {
}
