package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.service.RoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoomQueryServiceImpl implements RoomQueryService {

    private final RoomRepository roomRepository;
    private final RoomClosureRepository roomClosureRepository;

    @Override
    @Transactional(readOnly = true)
    public Room getActiveRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.of("ห้อง", roomId));
        if (!room.isActive()) {
            throw new BusinessRuleException("ห้อง " + room.getCode() + " ไม่เปิดให้จองในขณะนี้");
        }
        return room;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClosed(Long roomId, LocalDateTime start, LocalDateTime end) {
        return roomClosureRepository.existsOverlap(roomId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long roomId) {
        return roomRepository.existsById(roomId);
    }
}
