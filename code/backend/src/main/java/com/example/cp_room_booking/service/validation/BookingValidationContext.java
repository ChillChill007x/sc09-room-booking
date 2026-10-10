package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.service.policy.BookingPolicy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * ข้อมูลที่ส่งผ่าน handler ทุกตัวในสายตรวจ handler ใส่ผลที่หาได้ (room, policy) ให้ตัวถัดไปใช้ต่อ
 */
@Getter
@Builder
public class BookingValidationContext {

    private final Long userId;
    private final Role role;
    private final Long roomId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int attendees;

    /**
     * id ของการจองที่กำลังแก้ไข ถ้าเป็นการจองใหม่เป็น null
     */
    private final Long excludeBookingId;

    @Setter
    private Room room;

    @Setter
    private BookingPolicy policy;

    public boolean isUpdate() {
        return excludeBookingId != null;
    }

    public Duration duration() {
        return Duration.between(startTime, endTime);
    }
}
