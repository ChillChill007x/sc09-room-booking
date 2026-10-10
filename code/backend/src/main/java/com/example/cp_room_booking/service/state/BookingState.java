package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;

import java.util.Optional;

/**
 * State: แต่ละสถานะรู้เองว่ารับ action อะไรได้และไปสถานะไหนต่อ
 * next() คืน Optional.empty() เมื่อทำไม่ได้ แทนการโยน UnsupportedOperationException
 * ทุก state จึงแทนกันได้ตาม Liskov และ Service ตรวจ canHandle() ก่อนเสมอ
 */
public interface BookingState {

    BookingStatus status();

    boolean canHandle(BookingAction action);

    Optional<BookingStatus> next(BookingAction action);
}
