package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Notification;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.NotificationType;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.response.NotificationResponse;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.NotificationMapper;
import com.example.cp_room_booking.repository.NotificationRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.service.notification.NotificationMessage;
import com.example.cp_room_booking.service.notification.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.cp_room_booking.support.SecurityTestUtils.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationSender inApp;
    @Mock
    private NotificationSender email;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, userRepository, List.of(inApp, email),
                new NotificationMapper());
    }

    @Test
    void notifyUser_sendsThroughEverySender() {
        service.notifyUser(4L, 100L, NotificationType.BOOKING_APPROVED, "อนุมัติแล้ว");

        ArgumentCaptor<NotificationMessage> message = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(inApp).send(message.capture());
        verify(email).send(message.getValue());
        assertThat(message.getValue().recipientId()).isEqualTo(4L);
    }

    @Test
    void notifyStaff_sendsToEachActiveStaff() {
        when(userRepository.findAllByRoleInAndStatus(any(), any())).thenReturn(List.of(
                user(1L, Role.ADMIN), user(2L, Role.STAFF)));

        service.notifyStaff(100L, NotificationType.BOOKING_CREATED, "มีคำขอใหม่");

        ArgumentCaptor<NotificationMessage> messages = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(inApp, times(2)).send(messages.capture());
        assertThat(messages.getAllValues()).extracting(NotificationMessage::recipientId).containsExactly(1L, 2L);
    }

    @Test
    void markRead_ownNotification_setsRead() {
        Notification notification = notification(4L);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        NotificationResponse response = service.markRead(10L, principal(4L, Role.STUDENT));

        assertThat(response.read()).isTrue();
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markRead_otherUsersNotification_throwsForbidden() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification(4L)));

        assertThatThrownBy(() -> service.markRead(10L, principal(5L, Role.STUDENT)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void delete_otherUsersNotification_throwsForbiddenAndKeepsIt() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification(4L)));

        assertThatThrownBy(() -> service.delete(10L, principal(5L, Role.STAFF)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void markRead_missing_throwsNotFound() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(10L, principal(4L, Role.STUDENT)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Notification notification(Long ownerId) {
        return Notification.builder().id(10L).user(user(ownerId, Role.STUDENT))
                .type(NotificationType.BOOKING_APPROVED).message("อนุมัติแล้ว").read(false).build();
    }

    private User user(Long id, Role role) {
        return User.builder().id(id).email("u" + id + "@kkumail.com").passwordHash("x").role(role)
                .status(UserStatus.ACTIVE).build();
    }
}
