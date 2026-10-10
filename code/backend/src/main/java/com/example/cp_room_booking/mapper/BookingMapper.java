package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.dto.response.BookingSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Room room = booking.getRoom();
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(room.getId())
                .roomCode(room.getCode())
                .roomName(room.getName())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userFullName(user.getProfile() != null ? user.getProfile().getFullName() : null)
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .attendees(booking.getAttendees())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public BookingSlotResponse toSlot(Booking booking) {
        return BookingSlotResponse.builder()
                .id(booking.getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .build();
    }

    public Booking toEntity(BookingRequest request, User user, Room room) {
        return Booking.builder()
                .user(user)
                .room(room)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .purpose(request.purpose().trim())
                .attendees(request.attendees())
                .build();
    }

    public void updateEntity(Booking booking, BookingRequest request, Room room) {
        booking.setRoom(room);
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setPurpose(request.purpose().trim());
        booking.setAttendees(request.attendees());
    }
}
