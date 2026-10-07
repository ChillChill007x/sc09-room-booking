package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "กรุณากรอกชื่อ-นามสกุล")
        @Size(max = 150)
        String fullName,

        @Size(max = 20)
        String studentCode,

        @Pattern(regexp = "^$|^[0-9+\\-]{9,20}$", message = "รูปแบบเบอร์โทรไม่ถูกต้อง")
        String phone,

        @Size(max = 150)
        String department
) {
}
