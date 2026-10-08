package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ไม่มาใช้ห้อง: สถานะสุดท้าย
 */
@Component
public class NoShowState extends AbstractBookingState {

    public NoShowState() {
        super(BookingStatus.NO_SHOW, Map.of());
    }
}
