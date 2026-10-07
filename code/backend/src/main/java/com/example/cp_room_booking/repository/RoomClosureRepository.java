package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.RoomClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoomClosureRepository extends JpaRepository<RoomClosure, Long> {

    List<RoomClosure> findByRoomIdOrderByStartTimeAsc(Long roomId);

    Optional<RoomClosure> findByIdAndRoomId(Long id, Long roomId);

    /**
     * ช่วงเวลาทับกันเมื่อ start < :end และ end > :start ช่วงที่ต่อกันพอดีไม่นับว่าทับ
     */
    @Query("""
            select count(c) > 0 from RoomClosure c
            where c.room.id = :roomId and c.startTime < :end and c.endTime > :start
            """)
    boolean existsOverlap(Long roomId, LocalDateTime start, LocalDateTime end);

    @Query("""
            select distinct c.room.id from RoomClosure c
            where c.startTime < :end and c.endTime > :start
            """)
    Set<Long> findClosedRoomIds(LocalDateTime start, LocalDateTime end);
}
