package com.example.cp_room_booking.service.validation;

import org.springframework.stereotype.Component;

/**
 * ประกอบ handler ทั้ง 5 ตัวตามลำดับ ตรวจของถูกก่อน (เวลา กฎ role) แล้วค่อย query ฐานข้อมูล
 */
@Component
public class BookingValidationChain {

    private final BookingValidationHandler head;

    public BookingValidationChain(TimeRangeHandler timeRangeHandler,
                                  PolicyHandler policyHandler,
                                  RoomHandler roomHandler,
                                  ClosureHandler closureHandler,
                                  ConflictHandler conflictHandler) {
        timeRangeHandler
                .setNext(policyHandler)
                .setNext(roomHandler)
                .setNext(closureHandler)
                .setNext(conflictHandler);
        this.head = timeRangeHandler;
    }

    public void validate(BookingValidationContext context) {
        head.validate(context);
    }
}
