package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * role เว้นว่างได้ ระบบจะใช้ STUDENT สมัครเองได้เฉพาะ STUDENT และ LECTURER
 */
public record RegisterRequest(
        @NotBlank(message = "กรุณากรอกอีเมล")
        @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
        @Size(max = 150)
        String email,

        @NotBlank(message = "กรุณากรอกรหัสผ่าน")
        @Size(min = 8, max = 72, message = "รหัสผ่านต้องยาว 8 ถึง 72 ตัวอักษร")
        @MaxUtf8Bytes(value = 72, message = "รหัสผ่านยาวเกินไป (ภาษาไทย 1 ตัวนับเป็น 3 ไบต์ รวมได้ไม่เกิน 72 ไบต์)")
        String password,

        @NotBlank(message = "กรุณากรอกชื่อ-นามสกุล")
        @Size(max = 150)
        String fullName,

        @Size(max = 20)
        String studentCode,

        @Pattern(regexp = "^$|^[0-9+\\-]{9,20}$", message = "รูปแบบเบอร์โทรไม่ถูกต้อง")
        String phone,

        @Size(max = 150)
        String department,

        Role role
) {
}
