package com.example.cp_room_booking.service.notification;

/**
 * ช่องทางส่งแจ้งเตือน ตอนนี้มีแค่ในระบบ (InAppNotificationSender)
 * เพิ่มอีเมลหรือ LINE ได้ด้วยการเพิ่ม class ที่ implement interface นี้ ไม่ต้องแก้ NotificationService (Open/Closed)
 */
public interface NotificationSender {

    void send(NotificationMessage message);
}
