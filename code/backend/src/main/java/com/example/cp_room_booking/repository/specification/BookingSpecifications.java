package com.example.cp_room_booking.repository.specification;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * ตัวกรองของ GET /api/v1/bookings สำหรับเจ้าหน้าที่
 */
public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> hasRoom(Long roomId) {
        return (root, query, cb) -> cb.equal(root.get("room").get("id"), roomId);
    }

    public static Specification<Booking> startsAtOrAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), from);
    }

    public static Specification<Booking> startsBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThan(root.get("startTime"), to);
    }
}
