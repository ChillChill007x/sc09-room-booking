package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentRequest(
        @NotBlank(message = "กรุณากรอกชื่ออุปกรณ์")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
