package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.domain.enums.RoomStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoomRequest(
        @NotBlank(message = "กรุณากรอกรหัสห้อง")
        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "รหัสห้องใช้ได้เฉพาะตัวอักษรอังกฤษ ตัวเลข และ -")
        String code,

        @NotBlank(message = "กรุณากรอกชื่อห้อง")
        @Size(max = 150)
        String name,

        @NotNull(message = "กรุณากรอกชั้น")
        @Min(value = 1, message = "ชั้นต้องไม่ต่ำกว่า 1")
        @Max(value = 20, message = "ชั้นต้องไม่เกิน 20")
        Integer floor,

        @NotNull(message = "กรุณากรอกความจุ")
        @Min(value = 1, message = "ความจุต้องอย่างน้อย 1 คน")
        @Max(value = 1000, message = "ความจุต้องไม่เกิน 1000 คน")
        Integer capacity,

        @Size(max = 500)
        String description,

        @NotNull(message = "กรุณาเลือกประเภทห้อง")
        Long roomTypeId,

        RoomStatus status
) {
}
