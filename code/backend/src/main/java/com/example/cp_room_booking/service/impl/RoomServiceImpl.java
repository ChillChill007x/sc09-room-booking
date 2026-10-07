package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.entity.Equipment;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.dto.request.RoomEquipmentRequest;
import com.example.cp_room_booking.dto.request.RoomRequest;
import com.example.cp_room_booking.dto.request.RoomSearchRequest;
import com.example.cp_room_booking.dto.response.RoomResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.RoomMapper;
import com.example.cp_room_booking.repository.EquipmentRepository;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.RoomTypeRepository;
import com.example.cp_room_booking.service.BookingQueryService;
import com.example.cp_room_booking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasEquipment;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasFloor;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasStatus;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.hasType;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.idNotIn;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.minCapacity;
import static com.example.cp_room_booking.repository.specification.RoomSpecifications.notStatus;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final EquipmentRepository equipmentRepository;
    private final RoomClosureRepository roomClosureRepository;
    private final BookingQueryService bookingQueryService;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> search(RoomSearchRequest criteria, Pageable pageable) {
        List<Specification<Room>> specs = new ArrayList<>();
        specs.add(criteria.status() != null ? hasStatus(criteria.status()) : notStatus(RoomStatus.INACTIVE));
        if (criteria.floor() != null) {
            specs.add(hasFloor(criteria.floor()));
        }
        if (criteria.typeId() != null) {
            specs.add(hasType(criteria.typeId()));
        }
        if (criteria.minCapacity() != null) {
            specs.add(minCapacity(criteria.minCapacity()));
        }
        if (criteria.equipmentId() != null) {
            specs.add(hasEquipment(criteria.equipmentId()));
        }
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            specs.add(keyword(criteria.keyword()));
        }
        return PageResponse.from(roomRepository.findAll(Specification.allOf(specs), pageable)
                .map(roomMapper::toResponse));
    }

    /**
     * ห้องว่าง = ห้อง ACTIVE ที่ความจุและอุปกรณ์ตรงเงื่อนไข ไม่ถูกจอง และไม่อยู่ในช่วงปิด
     * ข้อมูลการจองถามผ่าน BookingQueryService ไม่เรียก BookingRepository ตรง
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailable(LocalDateTime start, LocalDateTime end, Integer minCapacity,
                                            List<Long> equipmentIds) {
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("เวลาเริ่มต้องมาก่อนเวลาสิ้นสุด");
        }
        Set<Long> unavailable = new HashSet<>(bookingQueryService.findBookedRoomIds(start, end));
        unavailable.addAll(roomClosureRepository.findClosedRoomIds(start, end));

        List<Specification<Room>> specs = new ArrayList<>();
        specs.add(hasStatus(RoomStatus.ACTIVE));
        if (minCapacity != null) {
            specs.add(minCapacity(minCapacity));
        }
        if (equipmentIds != null) {
            equipmentIds.forEach(id -> specs.add(hasEquipment(id)));
        }
        if (!unavailable.isEmpty()) {
            specs.add(idNotIn(unavailable));
        }
        return roomRepository.findAll(Specification.allOf(specs), Sort.by("floor", "code")).stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        return roomMapper.toResponse(findDetail(id));
    }

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("รหัสห้อง " + request.code() + " มีอยู่แล้ว");
        }
        Room room = roomMapper.toEntity(request, findRoomType(request.roomTypeId()));
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = findDetail(id);
        if (roomRepository.existsByCodeIgnoreCaseAndIdNot(request.code().trim(), id)) {
            throw new ConflictException("รหัสห้อง " + request.code() + " มีอยู่แล้ว");
        }
        roomMapper.updateEntity(room, request, findRoomType(request.roomTypeId()));
        return roomMapper.toResponse(room);
    }

    /**
     * ไม่ลบแถวจริงเพราะการจองในอดีตยังอ้างอิงห้องอยู่ เปลี่ยนเป็น INACTIVE แทน
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ห้อง", id));
        if (bookingQueryService.hasFutureBookings(id)) {
            throw new ConflictException("ห้อง " + room.getCode() + " ยังมีการจองในอนาคต ลบไม่ได้");
        }
        room.setStatus(RoomStatus.INACTIVE);
    }

    @Override
    @Transactional
    public RoomResponse updateEquipment(Long id, List<RoomEquipmentRequest> items) {
        Room room = findDetail(id);
        Set<Long> ids = items.stream().map(RoomEquipmentRequest::equipmentId).collect(Collectors.toSet());
        if (ids.size() != items.size()) {
            throw new BusinessRuleException("ระบุอุปกรณ์ซ้ำกัน");
        }
        Map<Long, Equipment> found = equipmentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Equipment::getId, Function.identity()));
        ids.stream()
                .filter(equipmentId -> !found.containsKey(equipmentId))
                .findFirst()
                .ifPresent(missing -> {
                    throw ResourceNotFoundException.of("อุปกรณ์", missing);
                });

        Map<Equipment, Integer> desired = new LinkedHashMap<>();
        items.forEach(item -> desired.put(found.get(item.equipmentId()), item.quantity()));
        room.syncEquipment(desired);
        return roomMapper.toResponse(room);
    }

    private Room findDetail(Long id) {
        return roomRepository.findDetailById(id).orElseThrow(() -> ResourceNotFoundException.of("ห้อง", id));
    }

    private RoomType findRoomType(Long id) {
        return roomTypeRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ประเภทห้อง", id));
    }
}
