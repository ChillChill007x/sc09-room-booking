package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.BookingStatusHistory;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.dto.response.BookingHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingHistoryMapper {

    private static final String SYSTEM = "ระบบ";

    public BookingHistoryResponse toResponse(BookingStatusHistory history) {
        User changedBy = history.getChangedBy();
        return BookingHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedById(changedBy != null ? changedBy.getId() : null)
                .changedByName(displayName(changedBy))
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .build();
    }

    private String displayName(User user) {
        if (user == null) {
            return SYSTEM;
        }
        return user.getProfile() != null ? user.getProfile().getFullName() : user.getEmail();
    }
}
