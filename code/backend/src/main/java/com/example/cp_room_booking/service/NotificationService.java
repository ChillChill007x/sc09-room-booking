package com.example.cp_room_booking.service;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.NotificationType;
import com.example.cp_room_booking.dto.response.NotificationResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void notifyUser(Long userId, Long bookingId, NotificationType type, String message);

    void notifyStaff(Long bookingId, NotificationType type, String message);

    PageResponse<NotificationResponse> findMine(Long userId, Pageable pageable);

    long countUnread(Long userId);

    NotificationResponse markRead(Long notificationId, UserPrincipal actor);

    int markAllRead(Long userId);

    void delete(Long notificationId, UserPrincipal actor);
}
