package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull(message = "กรุณาเลือกห้อง")
        Long roomId,

        @NotNull(message = "กรุณาระบุเวลาเริ่ม")
        LocalDateTime startTime,

        @NotNull(message = "กรุณาระบุเวลาสิ้นสุด")
        LocalDateTime endTime,

        @NotBlank(message = "กรุณาระบุวัตถุประสงค์")
        @Size(max = 300)
        String purpose,

        @NotNull(message = "กรุณาระบุจำนวนผู้เข้าร่วม")
        @Min(value = 1, message = "ผู้เข้าร่วมต้องอย่างน้อย 1 คน")
        @Max(value = 1000)
        Integer attendees
) {
}
