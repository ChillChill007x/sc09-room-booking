package com.example.cp_room_booking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoomEquipmentRequest(
        @NotNull(message = "กรุณาระบุอุปกรณ์")
        Long equipmentId,

        @NotNull(message = "กรุณาระบุจำนวน")
        @Min(value = 1, message = "จำนวนต้องอย่างน้อย 1")
        @Max(value = 500, message = "จำนวนต้องไม่เกิน 500")
        Integer quantity
) {
}
