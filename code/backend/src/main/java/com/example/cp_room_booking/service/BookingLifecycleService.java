package com.example.cp_room_booking.service;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.dto.response.AllowedActionsResponse;
import com.example.cp_room_booking.dto.response.BookingHistoryResponse;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.security.UserPrincipal;

import java.util.List;

public interface BookingLifecycleService {

    BookingResponse changeStatus(Long bookingId, BookingAction action, String note, UserPrincipal actor);

    List<BookingHistoryResponse> getHistory(Long bookingId, UserPrincipal actor);

    AllowedActionsResponse getAllowedActions(Long bookingId, UserPrincipal actor);

    /**
     * APPROVED ที่เลยเวลาเริ่ม 15 นาทีโดยไม่ check-in เปลี่ยนเป็น NO_SHOW คืนจำนวนที่เปลี่ยน
     */
    int markNoShows();

    /**
     * CHECKED_IN ที่เลยเวลาจบแล้วเปลี่ยนเป็น COMPLETED คืนจำนวนที่เปลี่ยน
     */
    int completeFinished();
}
