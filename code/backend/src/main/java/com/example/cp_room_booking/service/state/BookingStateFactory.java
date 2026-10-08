package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * คืน state object ตาม BookingStatus Spring ส่ง state ทุกตัวมาทาง constructor
 */
@Component
public class BookingStateFactory {

    private final Map<BookingStatus, BookingState> states = new EnumMap<>(BookingStatus.class);

    public BookingStateFactory(List<BookingState> stateList) {
        stateList.forEach(state -> states.put(state.status(), state));
        if (states.size() != BookingStatus.values().length) {
            throw new IllegalStateException("BookingState ไม่ครบทุก BookingStatus: " + states.keySet());
        }
    }

    public BookingState of(BookingStatus status) {
        return states.get(status);
    }
}
