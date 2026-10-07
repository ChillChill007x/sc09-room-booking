package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
