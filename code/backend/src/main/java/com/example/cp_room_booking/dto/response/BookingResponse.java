package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingResponse(
        Long id,
        Long roomId,
        String roomCode,
        String roomName,
        Long userId,
        String userEmail,
        String userFullName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose,
        Integer attendees,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
