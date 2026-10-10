package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select count(re) > 0 from RoomEquipment re where re.equipment.id = :equipmentId")
    boolean isInstalledInAnyRoom(Long equipmentId);
}
