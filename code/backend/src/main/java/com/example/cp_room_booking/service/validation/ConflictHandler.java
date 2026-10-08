package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ขั้นที่ 5: เวลาต้องไม่ชนกับการจองอื่นของห้องเดียวกัน ชนแล้วตอบ 409
 */
@Component
@RequiredArgsConstructor
public class ConflictHandler extends BookingValidationHandler {

    private static final long NO_BOOKING = -1L;

    private final BookingRepository bookingRepository;

    @Override
    protected void check(BookingValidationContext context) {
        Long excludeId = context.isUpdate() ? context.getExcludeBookingId() : NO_BOOKING;
        boolean overlap = bookingRepository.existsOverlap(context.getRoomId(), context.getStartTime(),
                context.getEndTime(), BookingStatus.ACTIVE, excludeId);
        if (overlap) {
            throw new ConflictException("ช่วงเวลานี้มีการจองห้องนี้แล้ว");
        }
    }
}
