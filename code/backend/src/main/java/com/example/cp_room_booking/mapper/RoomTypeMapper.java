package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.dto.request.RoomTypeRequest;
import com.example.cp_room_booking.dto.response.RoomTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class RoomTypeMapper {

    public RoomTypeResponse toResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .build();
    }

    public RoomType toEntity(RoomTypeRequest request) {
        RoomType roomType = new RoomType();
        updateEntity(roomType, request);
        return roomType;
    }

    public void updateEntity(RoomType roomType, RoomTypeRequest request) {
        roomType.setName(request.name().trim());
        roomType.setDescription(request.description());
    }
}
