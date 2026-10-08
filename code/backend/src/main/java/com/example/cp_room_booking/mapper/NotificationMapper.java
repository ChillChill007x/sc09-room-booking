package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.Notification;
import com.example.cp_room_booking.dto.response.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .bookingId(notification.getBooking() != null ? notification.getBooking().getId() : null)
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
