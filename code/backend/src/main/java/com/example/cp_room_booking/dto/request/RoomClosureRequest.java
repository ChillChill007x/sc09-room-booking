package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RoomClosureRequest(
        @NotNull(message = "กรุณาระบุเวลาเริ่ม")
        LocalDateTime startTime,

        @NotNull(message = "กรุณาระบุเวลาสิ้นสุด")
        LocalDateTime endTime,

        @NotBlank(message = "กรุณาระบุเหตุผล")
        @Size(max = 255)
        String reason
) {
}
