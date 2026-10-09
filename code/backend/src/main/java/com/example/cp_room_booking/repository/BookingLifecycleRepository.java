package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * ล็อกแถวการจอง (SELECT ... FOR UPDATE) ก่อนเปลี่ยนสถานะ กันเจ้าหน้าที่ 2 คนอนุมัติและปฏิเสธรายการเดียวกันพร้อมกัน
     * คำขอที่มาทีหลังจะรอจนคำขอแรก commit แล้วจึงอ่านสถานะล่าสุด
     */
    @Query(value = "SELECT id FROM bookings WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Long> lockById(Long id);

    /**
     * อ่านสถานะจากฐานข้อมูลตรงๆ (ไม่ใช้ค่าที่ค้างใน persistence context) ใช้หลังล็อกเพื่อตรวจว่ายังเป็นสถานะเดิม
     */
    @Query("select b.status from Booking b where b.id = :id")
    Optional<BookingStatus> findStatusById(Long id);
}
