package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * รออนุมัติ: อนุมัติ ปฏิเสธ หรือยกเลิกได้
 */
@Component
public class PendingState extends AbstractBookingState {

    public PendingState() {
        super(BookingStatus.PENDING, Map.of(
                BookingAction.APPROVE, BookingStatus.APPROVED,
                BookingAction.REJECT, BookingStatus.REJECTED,
                BookingAction.CANCEL, BookingStatus.CANCELLED));
    }
}
