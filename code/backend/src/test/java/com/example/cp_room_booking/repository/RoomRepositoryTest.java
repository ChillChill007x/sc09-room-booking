package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Equipment;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomEquipment;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasEquipment;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasFloor;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.minCapacity;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.notStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * ใช้ข้อมูลห้องจริงจาก V2__rooms.sql
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void specification_floorAndMinCapacity_filtersRooms() {
        Specification<Room> spec = Specification.allOf(hasFloor(2), minCapacity(55));

        assertThat(roomRepository.findAll(spec)).extracting(Room::getCode)
                .containsExactlyInAnyOrder("SC09-2201", "SC09-2202");
    }

    @Test
    void specification_hasEquipment_usesManyToManyLink() {
        Long conferenceId = equipmentByName("ระบบประชุมทางไกล").getId();

        assertThat(roomRepository.findAll(hasEquipment(conferenceId))).extracting(Room::getCode)
                .containsExactlyInAnyOrder("SC09-3303", "SC09-4401");
    }

    @Test
    void specification_keyword_matchesCodeOrNameIgnoringCase() {
        assertThat(roomRepository.findAll(keyword("sc09-11"))).hasSize(3);
        assertThat(roomRepository.findAll(keyword("ประชุม"))).extracting(Room::getCode)
                .containsExactlyInAnyOrder("SC09-3303", "SC09-4401");
    }

    @Test
    void findAll_pageAndSortByCapacityDesc() {
        Page<Room> page = roomRepository.findAll(notStatus(RoomStatus.INACTIVE),
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "capacity")));

        assertThat(page.getTotalElements()).isEqualTo(12);
        assertThat(page.getTotalPages()).isEqualTo(4);
        assertThat(page.getContent()).extracting(Room::getCode)
                .containsExactly("SC09-1101", "SC09-3302", "SC09-1102");
    }

    @Test
    void findDetailById_loadsEquipmentWithQuantity() {
        Room room = roomRepository.findDetailById(roomByCode("SC09-2201").getId()).orElseThrow();

        assertThat(room.getEquipment()).extracting(link -> link.getEquipment().getName(), RoomEquipment::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple("คอมพิวเตอร์", 60),
                        tuple("โปรเจกเตอร์", 1));
    }

    @Test
    void syncEquipment_updatesRemovesAndAddsLinks() {
        Room room = roomRepository.findDetailById(roomByCode("SC09-2201").getId()).orElseThrow();
        Map<Equipment, Integer> desired = new LinkedHashMap<>();
        desired.put(equipmentByName("คอมพิวเตอร์"), 55);
        desired.put(equipmentByName("ไวท์บอร์ด"), 2);

        room.syncEquipment(desired);
        roomRepository.saveAndFlush(room);
        entityManager.clear();

        Room reloaded = roomRepository.findDetailById(room.getId()).orElseThrow();
        assertThat(reloaded.getEquipment()).extracting(link -> link.getEquipment().getName(), RoomEquipment::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple("คอมพิวเตอร์", 55),
                        tuple("ไวท์บอร์ด", 2));
    }

    @Test
    void existsByCodeIgnoreCase_detectsDuplicateCode() {
        assertThat(roomRepository.existsByCodeIgnoreCase("sc09-1101")).isTrue();
        assertThat(roomRepository.existsByCodeIgnoreCase("SC09-0000")).isFalse();
    }

    private Room roomByCode(String code) {
        return roomRepository.findAll(keyword(code)).get(0);
    }

    private Equipment equipmentByName(String name) {
        return equipmentRepository.findAll().stream()
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
