package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.NotificationRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
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
 * หลายคนกดจองห้องเดียวกัน เวลาเดียวกัน พร้อมกันจริง (คนละ thread คนละ transaction บนฐานข้อมูลจริง)
 * ต้องสำเร็จได้รายการเดียว ที่เหลือได้ 409 เวลาชน
 */
@SpringBootTest
class BookingConcurrencyIntegrationTest {

    private static final List<String> BOOKERS = List.of(
            "student@kkumail.com", "student2@kkumail.com", "lecturer@kkumail.com",
            "staff@kkumail.com", "admin@kkumail.com");

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Clock clock;

    private final List<Long> createdIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll(notificationRepository.findAll().stream()
                .filter(n -> n.getBooking() != null && createdIds.contains(n.getBooking().getId()))
                .toList());
        bookingRepository.deleteAllById(createdIds);
        createdIds.clear();
    }

    @RepeatedTest(3)
    void sameRoomSameSlotAtOnce_onlyOneBookingSucceeds() throws Exception {
        Long roomId = roomRepository.findAll(keyword("SC09-9231")).get(0).getId();
        LocalDateTime start = LocalDate.now(clock).plusDays(3).atTime(13, 0);
        BookingRequest request = new BookingRequest(roomId, start, start.plusHours(1), "ทดสอบจองพร้อมกัน", 5);

        List<Callable<BookingResponse>> tasks = BOOKERS.stream()
                .map(email -> userRepository.findByEmail(email).orElseThrow())
                .map(UserPrincipal::from)
                .<Callable<BookingResponse>>map(actor -> () -> bookingService.create(actor, request))
                .toList();

        List<Object> results = runTogether(tasks);

        List<BookingResponse> created = results.stream()
                .filter(BookingResponse.class::isInstance).map(BookingResponse.class::cast).toList();
        created.forEach(booking -> createdIds.add(booking.id()));
        assertThat(created).hasSize(1);
        assertThat(results).filteredOn(ConflictException.class::isInstance).hasSize(BOOKERS.size() - 1);
        assertThat(bookingRepository.findAll()).filteredOn(b -> Objects.equals(b.getRoom().getId(), roomId)
                && b.getStartTime().equals(start)).hasSize(1);
    }

    /**
     * ปล่อยทุก task พร้อมกันด้วย latch แล้วคืนผลลัพธ์หรือ exception ของแต่ละ task
     */
    private <T> List<Object> runTogether(List<Callable<T>> tasks) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = tasks.stream()
                    .map(task -> pool.submit(() -> {
                        start.await();
                        return task.call();
                    }))
                    .toList();
            start.countDown();
            List<Object> results = new ArrayList<>();
            for (Future<T> future : futures) {
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
