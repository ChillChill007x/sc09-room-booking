package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * อนุมัติแล้ว: check-in ยกเลิก หรือถูกตั้งเป็นไม่มาใช้ห้องได้
 */
@Component
public class ApprovedState extends AbstractBookingState {

    public ApprovedState() {
        super(BookingStatus.APPROVED, Map.of(
                BookingAction.CHECK_IN, BookingStatus.CHECKED_IN,
                BookingAction.CANCEL, BookingStatus.CANCELLED,
                BookingAction.MARK_NO_SHOW, BookingStatus.NO_SHOW));
    }
}
