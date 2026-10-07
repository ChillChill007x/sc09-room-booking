package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.request.RoomTypeRequest;
import com.example.cp_room_booking.dto.response.RoomTypeResponse;

import java.util.List;

public interface RoomTypeService {

    List<RoomTypeResponse> findAll();

    RoomTypeResponse getById(Long id);

    RoomTypeResponse create(RoomTypeRequest request);

    RoomTypeResponse update(Long id, RoomTypeRequest request);

    void delete(Long id);
}
