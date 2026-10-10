package com.example.cp_room_booking.dto.request;

import com.example.cp_room_booking.domain.enums.BookingAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * note จำเป็นเมื่อ action เป็น REJECT
 */
public record StatusChangeRequest(
        @NotNull(message = "กรุณาระบุ action")
        BookingAction action,

        @Size(max = 500)
        String note
) {
}
