package com.example.cp_room_booking.event;

import com.example.cp_room_booking.domain.entity.Notification;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.NotificationType;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.NotificationRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingLifecycleService;
import com.example.cp_room_booking.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Observer ตั้งแต่ต้นจนจบ: สร้างการจองจริง publish event แล้ว listener สร้างแจ้งเตือนถึงคนที่ถูกต้องหลัง commit
 */
@SpringBootTest
class NotificationFlowIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingLifecycleService lifecycleService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Clock clock;

    private Long bookingId;

    @AfterEach
    void cleanUp() {
        if (bookingId != null) {
            notificationRepository.deleteAll(notificationsOf(bookingId));
            bookingRepository.deleteById(bookingId);
        }
    }

    @Test
    void createThenApprove_notifiesStaffThenOwner() {
        User student = userRepository.findByEmail("student2@kkumail.com").orElseThrow();
        User staff = userRepository.findByEmail("staff@kkumail.com").orElseThrow();
        User admin = userRepository.findByEmail("admin@kkumail.com").orElseThrow();
        Long roomId = roomRepository.findAll(keyword("SC09-2202")).get(0).getId();
        LocalDateTime start = LocalDate.now(clock).plusDays(2).atTime(15, 0);

        BookingResponse created = bookingService.create(UserPrincipal.from(student),
                new BookingRequest(roomId, start, start.plusHours(1), "ทดสอบแจ้งเตือน", 5));
        bookingId = created.id();

        assertThat(notificationsOf(bookingId))
                .filteredOn(n -> n.getType() == NotificationType.BOOKING_CREATED)
                .extracting(n -> n.getUser().getId())
                .containsExactlyInAnyOrder(staff.getId(), admin.getId());

        lifecycleService.changeStatus(bookingId, BookingAction.APPROVE, null,
                new UserPrincipal(staff.getId(), staff.getEmail(), "x", Role.STAFF, true));

        assertThat(notificationsOf(bookingId))
                .filteredOn(n -> n.getType() == NotificationType.BOOKING_APPROVED)
                .extracting(n -> n.getUser().getId())
                .containsExactly(student.getId());
    }

    private List<Notification> notificationsOf(Long id) {
        return notificationRepository.findAll().stream()
                .filter(n -> n.getBooking() != null && Objects.equals(n.getBooking().getId(), id))
                .toList();
    }
}
