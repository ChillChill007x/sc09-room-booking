package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.Equipment;
import com.example.cp_room_booking.dto.request.EquipmentRequest;
import com.example.cp_room_booking.dto.response.EquipmentResponse;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {

    public EquipmentResponse toResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .build();
    }

    public Equipment toEntity(EquipmentRequest request) {
        Equipment equipment = new Equipment();
        updateEntity(equipment, request);
        return equipment;
    }

    public void updateEntity(Equipment equipment, EquipmentRequest request) {
        equipment.setName(request.name().trim());
        equipment.setDescription(request.description());
    }
}
