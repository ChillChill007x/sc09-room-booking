package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ขั้นที่ 3: ห้องเปิดใช้งาน และความจุพอกับจำนวนผู้เข้าร่วม ถามผ่าน RoomQueryService ของคนที่ 2
 */
@Component
@RequiredArgsConstructor
public class RoomHandler extends BookingValidationHandler {

    private final RoomQueryService roomQueryService;

    @Override
    protected void check(BookingValidationContext context) {
        Room room = roomQueryService.getActiveRoom(context.getRoomId());
        if (context.getAttendees() > room.getCapacity()) {
            throw new BusinessRuleException("ห้อง " + room.getCode() + " รับได้ " + room.getCapacity()
                    + " คน แต่ผู้เข้าร่วม " + context.getAttendees() + " คน");
        }
        context.setRoom(room);
    }
}
