package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * เจ้าหน้าที่แก้ข้อมูลผู้ใช้ รวมถึง role และสถานะ
 */
public record UpdateUserRequest(
        @NotNull(message = "กรุณาเลือกบทบาท")
        Role role,

        @NotNull(message = "กรุณาเลือกสถานะ")
        UserStatus status,

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
