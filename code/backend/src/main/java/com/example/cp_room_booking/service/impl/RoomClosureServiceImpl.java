package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomClosure;
import com.example.cp_room_booking.dto.request.RoomClosureRequest;
import com.example.cp_room_booking.dto.response.RoomClosureResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.RoomMapper;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.service.RoomClosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomClosureServiceImpl implements RoomClosureService {

    private final RoomRepository roomRepository;
    private final RoomClosureRepository roomClosureRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoomClosureResponse> findByRoom(Long roomId) {
        requireRoom(roomId);
        return roomClosureRepository.findByRoomIdOrderByStartTimeAsc(roomId).stream()
                .map(roomMapper::toClosureResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoomClosureResponse create(Long roomId, RoomClosureRequest request) {
        Room room = requireRoom(roomId);
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessRuleException("เวลาเริ่มต้องมาก่อนเวลาสิ้นสุด");
        }
        if (roomClosureRepository.existsOverlap(roomId, request.startTime(), request.endTime())) {
            throw new ConflictException("ช่วงเวลานี้ทับกับช่วงปิดห้องที่มีอยู่แล้ว");
        }
        RoomClosure saved = roomClosureRepository.save(roomMapper.toClosure(room, request));
        return roomMapper.toClosureResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long roomId, Long closureId) {
        RoomClosure closure = roomClosureRepository.findByIdAndRoomId(closureId, roomId)
                .orElseThrow(() -> ResourceNotFoundException.of("ช่วงปิดห้อง", closureId));
        roomClosureRepository.delete(closure);
    }

    private Room requireRoom(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> ResourceNotFoundException.of("ห้อง", roomId));
    }
}
