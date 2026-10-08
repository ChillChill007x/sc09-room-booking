package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * query สำหรับสถิติ (คนที่ 5) อ่านอย่างเดียว แยกจาก BookingRepository ของคนที่ 3
 */
public interface BookingStatsRepository extends Repository<Booking, Long> {

    @Query("select b.status as status, count(b) as total from Booking b group by b.status")
    List<StatusCount> countByStatus();

    @Query("select count(b) from Booking b where b.startTime >= :from and b.startTime < :to")
    long countStartingBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
            select b from Booking b join fetch b.room
            where b.status in :statuses and b.startTime < :to and b.endTime > :from
            """)
    List<Booking> findUsage(LocalDateTime from, LocalDateTime to, Collection<BookingStatus> statuses);

    interface StatusCount {
        BookingStatus getStatus();

        long getTotal();
    }
}
