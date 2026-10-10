package com.example.cp_room_booking.service.notification;

import com.example.cp_room_booking.domain.enums.NotificationType;

/**
 * ข้อความแจ้งเตือนหนึ่งฉบับที่ส่งให้ผู้รับหนึ่งคน ไม่ผูกกับช่องทางการส่ง
 */
public record NotificationMessage(
        Long recipientId,
        Long bookingId,
        NotificationType type,
        String message
) {
}
