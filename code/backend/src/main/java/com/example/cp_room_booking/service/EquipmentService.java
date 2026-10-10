package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.request.EquipmentRequest;
import com.example.cp_room_booking.dto.response.EquipmentResponse;

import java.util.List;

public interface EquipmentService {

    List<EquipmentResponse> findAll();

    EquipmentResponse getById(Long id);

    EquipmentResponse create(EquipmentRequest request);

    EquipmentResponse update(Long id, EquipmentRequest request);

    void delete(Long id);
}
