package com.example.cp_room_booking.service.validation;

/**
 * Chain of Responsibility: แต่ละ handler ตรวจกฎของตัวเอง ผ่านแล้วส่งต่อให้ตัวถัดไป ไม่ผ่านโยน exception
 * เพิ่มกฎใหม่ได้ด้วยการเพิ่ม handler แล้วต่อเข้า chain โดยไม่ต้องแก้ handler เดิม
 */
public abstract class BookingValidationHandler {

    private BookingValidationHandler next;

    /**
     * คืน handler ถัดไปเพื่อให้ต่อ chain แบบ a.setNext(b).setNext(c) ได้
     */
    public BookingValidationHandler setNext(BookingValidationHandler next) {
        this.next = next;
        return next;
    }

    public void validate(BookingValidationContext context) {
        check(context);
        if (next != null) {
            next.validate(context);
        }
    }

    protected abstract void check(BookingValidationContext context);
}
