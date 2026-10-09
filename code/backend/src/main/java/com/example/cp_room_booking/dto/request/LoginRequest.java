package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.dto.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "กรุณากรอกอีเมล")
        @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
        String email,

        // BCrypt ตรวจรหัสที่เกิน 72 ไบต์ไม่ได้ (โยน exception) จึงตัดออกตั้งแต่ validation
        @NotBlank(message = "กรุณากรอกรหัสผ่าน")
        @MaxUtf8Bytes(value = 72, message = "อีเมลหรือรหัสผ่านไม่ถูกต้อง")
        String password
) {
}
