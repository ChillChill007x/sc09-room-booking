package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ยกเลิกแล้ว: สถานะสุดท้าย
 */
@Component
public class CancelledState extends AbstractBookingState {

    public CancelledState() {
        super(BookingStatus.CANCELLED, Map.of());
    }
}
