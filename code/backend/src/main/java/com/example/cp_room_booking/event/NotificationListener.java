package com.example.cp_room_booking.event;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.NotificationType;
import com.example.cp_room_booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Observer: รับ event ของการจองแล้วสร้างแจ้งเตือน โมดูลการจองไม่ต้องรู้จักโมดูลแจ้งเตือน
 * ทำงานหลัง commit (AFTER_COMMIT) จึงไม่แจ้งเตือนถ้าการจองถูก rollback
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<BookingStatus, NotificationType> OWNER_NOTIFICATIONS = new EnumMap<>(Map.of(
            BookingStatus.APPROVED, NotificationType.BOOKING_APPROVED,
            BookingStatus.REJECTED, NotificationType.BOOKING_REJECTED,
            BookingStatus.CANCELLED, NotificationType.BOOKING_CANCELLED,
            BookingStatus.NO_SHOW, NotificationType.BOOKING_NO_SHOW));

    private static final Map<BookingStatus, String> STATUS_TEXT = new EnumMap<>(Map.of(
            BookingStatus.APPROVED, "ได้รับการอนุมัติแล้ว",
            BookingStatus.REJECTED, "ถูกปฏิเสธ",
            BookingStatus.CANCELLED, "ถูกยกเลิก",
            BookingStatus.NO_SHOW, "ถูกบันทึกว่าไม่มาใช้ห้อง"));

    private final NotificationService notificationService;

    /**
     * การจองใหม่: แจ้งเจ้าหน้าที่ทุกคน
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        String message = event.status() == BookingStatus.PENDING
                ? "มีคำขอจองห้อง " + event.roomCode() + " วันที่ " + format(event.startTime()) + " รออนุมัติ"
                : "มีการจองห้อง " + event.roomCode() + " วันที่ " + format(event.startTime()) + " (อนุมัติอัตโนมัติ)";
        safely(() -> notificationService.notifyStaff(event.bookingId(), NotificationType.BOOKING_CREATED, message));
    }

    /**
     * อนุมัติ ปฏิเสธ ยกเลิก และ NO_SHOW: แจ้งผู้จอง สถานะอื่นไม่แจ้ง
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(BookingStatusChangedEvent event) {
        Optional.ofNullable(OWNER_NOTIFICATIONS.get(event.toStatus())).ifPresent(type -> {
            String message = "การจองห้อง " + event.roomCode() + " วันที่ " + format(event.startTime()) + " "
                    + STATUS_TEXT.get(event.toStatus())
                    + (event.note() != null && !event.note().isBlank() ? " หมายเหตุ: " + event.note() : "");
            safely(() -> notificationService.notifyUser(event.userId(), event.bookingId(), type, message));
        });
    }

    /**
     * แจ้งเตือนล้มเหลวต้องไม่ทำให้การจองที่ commit ไปแล้วตอบ error
     */
    private void safely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            log.error("สร้างแจ้งเตือนไม่สำเร็จ", ex);
        }
    }

    private String format(LocalDateTime time) {
        return time.format(FORMAT);
    }
}
