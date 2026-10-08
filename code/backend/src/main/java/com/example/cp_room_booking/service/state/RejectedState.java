package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ถูกปฏิเสธ: สถานะสุดท้าย ไม่รับ action ใดอีก
 */
@Component
public class RejectedState extends AbstractBookingState {

    public RejectedState() {
        super(BookingStatus.REJECTED, Map.of());
    }
}
