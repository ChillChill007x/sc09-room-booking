package com.example.cp_room_booking.service.notification;

import com.example.cp_room_booking.domain.entity.Notification;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.NotificationRepository;
import com.example.cp_room_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * แจ้งเตือนในระบบ: บันทึกลงตาราง notifications ให้ผู้ใช้เห็นที่กระดิ่งและหน้า /notifications
 */
@Component
@RequiredArgsConstructor
public class InAppNotificationSender implements NotificationSender {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void send(NotificationMessage message) {
        notificationRepository.save(Notification.builder()
                .user(userRepository.getReferenceById(message.recipientId()))
                .booking(message.bookingId() != null ? bookingRepository.getReferenceById(message.bookingId()) : null)
                .type(message.type())
                .message(message.message())
                .read(false)
                .build());
    }
}
