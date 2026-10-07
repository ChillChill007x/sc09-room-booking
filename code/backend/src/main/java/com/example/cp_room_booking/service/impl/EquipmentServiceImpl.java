package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Equipment;
import com.example.cp_room_booking.dto.request.EquipmentRequest;
import com.example.cp_room_booking.dto.response.EquipmentResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.EquipmentMapper;
import com.example.cp_room_booking.repository.EquipmentRepository;
import com.example.cp_room_booking.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return equipmentRepository.findAll(Sort.by("name")).stream().map(equipmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getById(Long id) {
        return equipmentMapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        if (equipmentRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("อุปกรณ์ชื่อ " + request.name() + " มีอยู่แล้ว");
        }
        return equipmentMapper.toResponse(equipmentRepository.save(equipmentMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = find(id);
        if (equipmentRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("อุปกรณ์ชื่อ " + request.name() + " มีอยู่แล้ว");
        }
        equipmentMapper.updateEntity(equipment, request);
        return equipmentMapper.toResponse(equipment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Equipment equipment = find(id);
        if (equipmentRepository.isInstalledInAnyRoom(id)) {
            throw new ConflictException("อุปกรณ์ " + equipment.getName() + " ยังติดตั้งอยู่ในห้อง ลบไม่ได้");
        }
        equipmentRepository.delete(equipment);
    }

    private Equipment find(Long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("อุปกรณ์", id));
    }
}
