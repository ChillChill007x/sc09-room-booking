package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.request.RoomClosureRequest;
import com.example.cp_room_booking.dto.response.RoomClosureResponse;

import java.util.List;

public interface RoomClosureService {

    List<RoomClosureResponse> findByRoom(Long roomId);

    RoomClosureResponse create(Long roomId, RoomClosureRequest request);

    void delete(Long roomId, Long closureId);
}
