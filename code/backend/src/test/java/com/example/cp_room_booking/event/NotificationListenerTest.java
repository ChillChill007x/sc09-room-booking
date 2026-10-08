package com.example.cp_room_booking.event;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.NotificationType;
import com.example.cp_room_booking.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 9, 0);

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationListener listener;

    @Test
    void bookingCreatedPending_notifiesStaffAwaitingApproval() {
        listener.onBookingCreated(new BookingCreatedEvent(100L, 4L, "SC09-2201", START, START.plusHours(2),
                BookingStatus.PENDING));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifyStaff(eq(100L), eq(NotificationType.BOOKING_CREATED), message.capture());
        assertThat(message.getValue()).contains("SC09-2201", "15/01/2030 09:00", "รออนุมัติ");
    }

    @ParameterizedTest(name = "{0} แจ้งผู้จองด้วย {1}")
    @CsvSource({
            "APPROVED, BOOKING_APPROVED",
            "REJECTED, BOOKING_REJECTED",
            "CANCELLED, BOOKING_CANCELLED",
            "NO_SHOW, BOOKING_NO_SHOW"
    })
    void statusChanged_notifiesOwner(BookingStatus toStatus, NotificationType expectedType) {
        listener.onStatusChanged(new BookingStatusChangedEvent(100L, 4L, BookingStatus.PENDING, toStatus, null,
                "SC09-2201", START));

        verify(notificationService).notifyUser(eq(4L), eq(100L), eq(expectedType), anyString());
    }

    @Test
    void rejectedWithNote_includesReasonInMessage() {
        listener.onStatusChanged(new BookingStatusChangedEvent(100L, 4L, BookingStatus.PENDING,
                BookingStatus.REJECTED, "ห้องใช้จัดสอบ", "SC09-2201", START));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifyUser(eq(4L), eq(100L), eq(NotificationType.BOOKING_REJECTED),
                message.capture());
        assertThat(message.getValue()).contains("ถูกปฏิเสธ", "ห้องใช้จัดสอบ");
    }

    @Test
    void checkedIn_doesNotNotify() {
        listener.onStatusChanged(new BookingStatusChangedEvent(100L, 4L, BookingStatus.APPROVED,
                BookingStatus.CHECKED_IN, null, "SC09-2201", START));

        verifyNoInteractions(notificationService);
    }

    @Test
    void notificationFailure_doesNotPropagate() {
        doThrow(new IllegalStateException("db down")).when(notificationService)
                .notifyStaff(any(), any(), anyString());

        assertThatCode(() -> listener.onBookingCreated(new BookingCreatedEvent(100L, 4L, "SC09-2201", START,
                START.plusHours(1), BookingStatus.PENDING))).doesNotThrowAnyException();
    }
}
