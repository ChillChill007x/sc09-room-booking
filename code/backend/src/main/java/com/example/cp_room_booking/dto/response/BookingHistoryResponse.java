package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingHistoryResponse(
        Long id,
        BookingStatus fromStatus,
        BookingStatus toStatus,
        Long changedById,
        String changedByName,
        String note,
        LocalDateTime changedAt
) {
}
