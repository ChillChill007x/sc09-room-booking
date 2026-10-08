package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ขั้นที่ 4: ช่วงเวลาที่จองต้องไม่อยู่ในช่วงปิดห้อง
 */
@Component
@RequiredArgsConstructor
public class ClosureHandler extends BookingValidationHandler {

    private final RoomQueryService roomQueryService;

    @Override
    protected void check(BookingValidationContext context) {
        if (roomQueryService.isClosed(context.getRoomId(), context.getStartTime(), context.getEndTime())) {
            throw new ConflictException("ห้องปิดใช้งานในช่วงเวลาที่เลือก");
        }
    }
}
