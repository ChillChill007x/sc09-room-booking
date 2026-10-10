package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomTypeRequest(
        @NotBlank(message = "กรุณากรอกชื่อประเภทห้อง")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
