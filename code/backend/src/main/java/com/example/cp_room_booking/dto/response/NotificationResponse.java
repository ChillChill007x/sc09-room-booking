package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        Long bookingId,
        NotificationType type,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
}
