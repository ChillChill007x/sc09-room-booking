package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomClosure;
import com.example.cp_room_booking.domain.entity.RoomEquipment;
import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.dto.request.RoomClosureRequest;
import com.example.cp_room_booking.dto.request.RoomRequest;
import com.example.cp_room_booking.dto.response.RoomClosureResponse;
import com.example.cp_room_booking.dto.response.RoomEquipmentResponse;
import com.example.cp_room_booking.dto.response.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final RoomTypeMapper roomTypeMapper;

    public RoomResponse toResponse(Room room) {
        List<RoomEquipmentResponse> equipment = room.getEquipment().stream()
                .map(this::toEquipmentResponse)
                .sorted(Comparator.comparing(RoomEquipmentResponse::name))
                .toList();
        return RoomResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .floor(room.getFloor())
                .capacity(room.getCapacity())
                .description(room.getDescription())
                .status(room.getStatus())
                .roomType(roomTypeMapper.toResponse(room.getRoomType()))
                .equipment(equipment)
                .build();
    }

    public Room toEntity(RoomRequest request, RoomType roomType) {
        Room room = new Room();
        updateEntity(room, request, roomType);
        return room;
    }

    public void updateEntity(Room room, RoomRequest request, RoomType roomType) {
        room.setCode(request.code().trim().toUpperCase());
        room.setName(request.name().trim());
        room.setFloor(request.floor());
        room.setCapacity(request.capacity());
        room.setDescription(request.description());
        room.setRoomType(roomType);
        room.setStatus(request.status() == null ? RoomStatus.ACTIVE : request.status());
    }

    public RoomClosure toClosure(Room room, RoomClosureRequest request) {
        return RoomClosure.builder()
                .room(room)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .reason(request.reason().trim())
                .build();
    }

    public RoomClosureResponse toClosureResponse(RoomClosure closure) {
        return RoomClosureResponse.builder()
                .id(closure.getId())
                .roomId(closure.getRoom().getId())
                .startTime(closure.getStartTime())
                .endTime(closure.getEndTime())
                .reason(closure.getReason())
                .createdAt(closure.getCreatedAt())
                .build();
    }

    private RoomEquipmentResponse toEquipmentResponse(RoomEquipment link) {
        return RoomEquipmentResponse.builder()
                .equipmentId(link.getEquipment().getId())
                .name(link.getEquipment().getName())
                .quantity(link.getQuantity())
                .build();
    }
}
