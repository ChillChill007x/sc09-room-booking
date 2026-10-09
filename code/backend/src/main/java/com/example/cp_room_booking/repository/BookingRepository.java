package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /*
     * ล็อกแถว (SELECT ... FOR UPDATE) กันคำขอพร้อมกันผ่านการตรวจกฎทั้งคู่ เช่น จองห้องเดียวกันเวลาเดียวกัน
     * คำขอที่มาทีหลังจะรอจนคำขอแรก commit แล้วจึงตรวจเวลาชนจากข้อมูลล่าสุด
     * ลำดับการล็อกต้องเหมือนกันทุกที่: ผู้ใช้ -> ห้อง -> การจอง เพื่อไม่ให้เกิด deadlock
     * ใช้ native query เลือกเฉพาะ id จึงไม่ join ตารางอื่น (PostgreSQL ห้าม FOR UPDATE บน outer join)
     */
    @Query(value = "SELECT id FROM users WHERE id = :userId FOR UPDATE", nativeQuery = true)
    Optional<Long> lockUser(Long userId);

    @Query(value = "SELECT id FROM rooms WHERE id = :roomId FOR UPDATE", nativeQuery = true)
    Optional<Long> lockRoom(Long roomId);

    @Query(value = "SELECT id FROM bookings WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Long> lockBooking(Long id);

    /**
     * ตรวจเวลาชน: ห้องเดียวกัน สถานะยังใช้งาน และ start < :end AND end > :start
     * การจองที่เวลาต่อกันพอดี (จบ 10:00 เริ่ม 10:00) ไม่ถือว่าชน
     * excludeId ใช้ตอนแก้ไขการจอง เพื่อไม่ให้ชนกับตัวเอง (ส่ง -1 ถ้าเป็นการจองใหม่)
     */
    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId
              and b.status in :statuses
              and b.startTime < :end
              and b.endTime > :start
              and b.id <> :excludeId
            """)
    boolean existsOverlap(Long roomId, LocalDateTime start, LocalDateTime end,
                          Collection<BookingStatus> statuses, Long excludeId);

    @Query("""
            select distinct b.room.id from Booking b
            where b.status in :statuses and b.startTime < :end and b.endTime > :start
            """)
    Set<Long> findBookedRoomIds(LocalDateTime start, LocalDateTime end, Collection<BookingStatus> statuses);

    boolean existsByRoomIdAndStatusInAndEndTimeAfter(Long roomId, Collection<BookingStatus> statuses,
                                                     LocalDateTime now);

    long countByUserIdAndStatusInAndEndTimeAfter(Long userId, Collection<BookingStatus> statuses,
                                                 LocalDateTime now);

    @EntityGraph(attributePaths = {"user", "user.profile", "room"})
    Optional<Booking> findDetailById(Long id);

    @EntityGraph(attributePaths = {"user", "user.profile", "room"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "user.profile", "room"})
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @Query("""
            select b from Booking b
            where b.room.id = :roomId
              and b.status in :statuses
              and b.startTime < :to
              and b.endTime > :from
            order by b.startTime asc
            """)
    List<Booking> findRoomSchedule(Long roomId, LocalDateTime from, LocalDateTime to,
                                   Collection<BookingStatus> statuses);

    /**
     * การจองทุกห้องในช่วงเวลา ใช้กับปฏิทินรายเดือนและตารางรวมทุกห้องรายวัน
     */
    @Query("""
            select b from Booking b join fetch b.room
            where b.status in :statuses
              and b.startTime < :to
              and b.endTime > :from
            order by b.startTime asc
            """)
    List<Booking> findAllInRange(LocalDateTime from, LocalDateTime to, Collection<BookingStatus> statuses);
}
