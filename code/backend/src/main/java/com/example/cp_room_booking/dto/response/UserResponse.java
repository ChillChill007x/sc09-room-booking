package com.example.cp_room_booking.dto.response;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String email,
        Role role,
        UserStatus status,
        String fullName,
        String studentCode,
        String phone,
        String department,
        LocalDateTime createdAt
) {
}
