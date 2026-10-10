package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.dto.request.RoomTypeRequest;
import com.example.cp_room_booking.dto.response.RoomTypeResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.RoomTypeMapper;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.RoomTypeRepository;
import com.example.cp_room_booking.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeMapper roomTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> findAll() {
        return roomTypeRepository.findAll(Sort.by("name")).stream().map(roomTypeMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getById(Long id) {
        return roomTypeMapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public RoomTypeResponse create(RoomTypeRequest request) {
        if (roomTypeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("ประเภทห้อง " + request.name() + " มีอยู่แล้ว");
        }
        return roomTypeMapper.toResponse(roomTypeRepository.save(roomTypeMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public RoomTypeResponse update(Long id, RoomTypeRequest request) {
        RoomType roomType = find(id);
        if (roomTypeRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("ประเภทห้อง " + request.name() + " มีอยู่แล้ว");
        }
        roomTypeMapper.updateEntity(roomType, request);
        return roomTypeMapper.toResponse(roomType);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RoomType roomType = find(id);
        if (roomRepository.existsByRoomTypeId(id)) {
            throw new ConflictException("ยังมีห้องที่ใช้ประเภท " + roomType.getName() + " อยู่ ลบไม่ได้");
        }
        roomTypeRepository.delete(roomType);
    }

    private RoomType find(Long id) {
        return roomTypeRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ประเภทห้อง", id));
    }
}
