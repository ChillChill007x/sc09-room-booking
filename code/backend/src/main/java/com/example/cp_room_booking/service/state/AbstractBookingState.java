package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;

import java.util.Map;
import java.util.Optional;

/**
 * เก็บตารางการเปลี่ยนสถานะของ state หนึ่ง ๆ class ลูกแค่ประกาศว่ารับ action อะไรได้
 */
public abstract class AbstractBookingState implements BookingState {

    private final BookingStatus status;
    private final Map<BookingAction, BookingStatus> transitions;

    protected AbstractBookingState(BookingStatus status, Map<BookingAction, BookingStatus> transitions) {
        this.status = status;
        this.transitions = Map.copyOf(transitions);
    }

    @Override
    public BookingStatus status() {
        return status;
    }

    @Override
    public boolean canHandle(BookingAction action) {
        return transitions.containsKey(action);
    }

    @Override
    public Optional<BookingStatus> next(BookingAction action) {
        return Optional.ofNullable(transitions.get(action));
    }
}
