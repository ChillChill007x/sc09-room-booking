package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.BookingStatusHistory;
import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.exception.InvalidBookingStateException;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.BookingStatusHistoryRepository;
import com.example.cp_room_booking.repository.NotificationRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingLifecycleService;
import com.example.cp_room_booking.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * เจ้าหน้าที่ 2 คนกดอนุมัติและปฏิเสธการจองเดียวกันพร้อมกันจริง ต้องสำเร็จได้คนเดียว
 * อีกคนได้ 409 เพราะสถานะเปลี่ยนไปแล้ว และประวัติมีการเปลี่ยนจาก PENDING เพียงครั้งเดียว
 */
@SpringBootTest
class BookingStatusConcurrencyIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingLifecycleService lifecycleService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingStatusHistoryRepository historyRepository;
    @Autowired
    private NotificationRepository notificationRepository;
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
            notificationRepository.deleteAll(notificationRepository.findAll().stream()
                    .filter(n -> n.getBooking() != null && Objects.equals(n.getBooking().getId(), bookingId))
                    .toList());
            bookingRepository.deleteById(bookingId);
            bookingId = null;
        }
    }

    @RepeatedTest(3)
    void approveAndRejectAtOnce_onlyOneTransitionHappens() throws Exception {
        UserPrincipal student = actor("student2@kkumail.com");
        UserPrincipal staff = actor("staff@kkumail.com");
        UserPrincipal admin = actor("admin@kkumail.com");
        Long roomId = roomRepository.findAll(keyword("SC09-9231")).get(0).getId();
        LocalDateTime start = LocalDate.now(clock).plusDays(4).atTime(10, 0);
        BookingResponse pending = bookingService.create(student,
                new BookingRequest(roomId, start, start.plusHours(1), "ทดสอบอนุมัติพร้อมกัน", 5));
        bookingId = pending.id();
        assertThat(pending.status()).isEqualTo(BookingStatus.PENDING);

        List<Object> results = runTogether(List.of(
                () -> lifecycleService.changeStatus(bookingId, BookingAction.APPROVE, null, staff),
                () -> lifecycleService.changeStatus(bookingId, BookingAction.REJECT, "ทดสอบปฏิเสธ", admin)));

        assertThat(results).filteredOn(BookingResponse.class::isInstance).hasSize(1);
        assertThat(results).filteredOn(InvalidBookingStateException.class::isInstance).hasSize(1);
        List<BookingStatusHistory> fromPending = historyRepository.findByBookingIdOrderByChangedAtAscIdAsc(bookingId)
                .stream().filter(h -> h.getFromStatus() == BookingStatus.PENDING).toList();
        assertThat(fromPending).hasSize(1);
        BookingStatus winner = ((BookingResponse) results.stream()
                .filter(BookingResponse.class::isInstance).findFirst().orElseThrow()).status();
        assertThat(bookingRepository.findById(bookingId).orElseThrow().getStatus()).isEqualTo(winner);
    }

    private UserPrincipal actor(String email) {
        return UserPrincipal.from(userRepository.findByEmail(email).orElseThrow());
    }

    private List<Object> runTogether(List<Callable<BookingResponse>> tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<BookingResponse>> futures = tasks.stream()
                    .map(task -> pool.submit(() -> {
                        start.await();
                        return task.call();
                    }))
                    .toList();
            start.countDown();
            List<Object> results = new ArrayList<>();
            for (Future<BookingResponse> future : futures) {
                try {
                    results.add(future.get(30, TimeUnit.SECONDS));
                } catch (ExecutionException e) {
                    results.add(e.getCause());
                } catch (TimeoutException e) {
                    results.add(e);
                }
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}
