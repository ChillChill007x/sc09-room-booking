package com.example.cp_room_booking.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
