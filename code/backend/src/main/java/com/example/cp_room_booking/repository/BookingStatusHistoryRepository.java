package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

    @EntityGraph(attributePaths = {"changedBy", "changedBy.profile"})
    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAscIdAsc(Long bookingId);
}
