package com.example.cp_room_booking.service;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.dto.request.RoomEquipmentRequest;
import com.example.cp_room_booking.dto.request.RoomRequest;
import com.example.cp_room_booking.dto.request.RoomSearchRequest;
import com.example.cp_room_booking.dto.response.RoomResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {

    PageResponse<RoomResponse> search(RoomSearchRequest criteria, Pageable pageable);

    List<RoomResponse> findAvailable(LocalDateTime start, LocalDateTime end, Integer minCapacity,
                                     List<Long> equipmentIds);

    RoomResponse getById(Long id);

    RoomResponse create(RoomRequest request);

    RoomResponse update(Long id, RoomRequest request);

    void delete(Long id);

    RoomResponse updateEquipment(Long id, List<RoomEquipmentRequest> items);
}
