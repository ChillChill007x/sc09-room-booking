package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.common.PageResponse;
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
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.NotificationService;
import com.example.cp_room_booking.service.notification.NotificationMessage;
import com.example.cp_room_booking.service.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Set<Role> STAFF_ROLES = Set.of(Role.STAFF, Role.ADMIN);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final List<NotificationSender> senders;
    private final NotificationMapper notificationMapper;

    /**
     * ถูกเรียกจาก listener หลัง transaction ของการจอง commit แล้ว จึงต้องเปิด transaction ใหม่ (REQUIRES_NEW)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(Long userId, Long bookingId, NotificationType type, String message) {
        NotificationMessage notification = new NotificationMessage(userId, bookingId, type, message);
        senders.forEach(sender -> sender.send(notification));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyStaff(Long bookingId, NotificationType type, String message) {
        List<User> staff = userRepository.findAllByRoleInAndStatus(STAFF_ROLES, UserStatus.ACTIVE);
        staff.forEach(user -> {
            NotificationMessage notification = new NotificationMessage(user.getId(), bookingId, type, message);
            senders.forEach(sender -> sender.send(notification));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findMine(Long userId, Pageable pageable) {
        return PageResponse.from(notificationRepository.findByUserId(userId, pageable)
                .map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long notificationId, UserPrincipal actor) {
        Notification notification = findOwned(notificationId, actor);
        notification.setRead(true);
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }

    @Override
    @Transactional
    public void delete(Long notificationId, UserPrincipal actor) {
        notificationRepository.delete(findOwned(notificationId, actor));
    }

    private Notification findOwned(Long notificationId, UserPrincipal actor) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ResourceNotFoundException.of("แจ้งเตือน", notificationId));
        if (!notification.belongsTo(actor.getId())) {
            throw new ForbiddenOperationException("จัดการได้เฉพาะแจ้งเตือนของตัวเอง");
        }
        return notification;
    }
}
