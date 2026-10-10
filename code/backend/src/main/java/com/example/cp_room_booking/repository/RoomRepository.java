package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    @Override
    @EntityGraph(attributePaths = "roomType")
    Page<Room> findAll(Specification<Room> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "roomType")
    List<Room> findAll(Specification<Room> spec);

    @EntityGraph(attributePaths = {"roomType", "equipment", "equipment.equipment"})
    Optional<Room> findDetailById(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByRoomTypeId(Long roomTypeId);

    /**
     * ล็อกแถวห้อง (SELECT ... FOR UPDATE) ระหว่างสร้างช่วงปิดห้อง แถวเดียวกับที่การสร้างการจองล็อก
     * จึงสร้างช่วงปิดกับจองห้องพร้อมกันแล้วทับกันไม่ได้
     */
    @Query(value = "SELECT id FROM rooms WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Long> lockById(Long id);
}
