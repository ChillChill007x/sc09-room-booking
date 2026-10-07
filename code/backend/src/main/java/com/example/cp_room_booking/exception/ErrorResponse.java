package com.example.cp_room_booking.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * รูปแบบ error ที่ทุก endpoint ตอบกลับเหมือนกัน
 */
@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {

    @Builder
    public record FieldErrorDetail(String field, String message) {
    }
}
