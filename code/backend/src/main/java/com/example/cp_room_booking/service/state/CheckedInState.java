package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * กำลังใช้ห้อง: จบการใช้งานได้อย่างเดียว
 */
@Component
public class CheckedInState extends AbstractBookingState {

    public CheckedInState() {
        super(BookingStatus.CHECKED_IN, Map.of(BookingAction.COMPLETE, BookingStatus.COMPLETED));
    }
}
