package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ใช้ห้องเสร็จแล้ว: สถานะสุดท้าย
 */
@Component
public class CompletedState extends AbstractBookingState {

    public CompletedState() {
        super(BookingStatus.COMPLETED, Map.of());
    }
}
