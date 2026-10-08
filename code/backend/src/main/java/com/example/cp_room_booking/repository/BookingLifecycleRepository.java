package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * query ที่ใช้เฉพาะงานวงจรสถานะ (คนที่ 4) แยกจาก BookingRepository ของคนที่ 3 เพื่อไม่แก้ไฟล์ของคนอื่น
 */
public interface BookingLifecycleRepository extends Repository<Booking, Long> {

    @Query("""
            select b from Booking b join fetch b.room join fetch b.user
            where b.status = :status and b.startTime < :threshold
            """)
    List<Booking> findByStatusAndStartTimeBefore(BookingStatus status, LocalDateTime threshold);

    @Query("""
            select b from Booking b join fetch b.room join fetch b.user
            where b.status = :status and b.endTime < :threshold
            """)
    List<Booking> findByStatusAndEndTimeBefore(BookingStatus status, LocalDateTime threshold);
}
