package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * การจองที่มีอยู่ 10:00-12:00 แล้วตรวจกรณีชนเต็มช่วง ชนบางส่วน อยู่ข้างใน และต่อกันพอดี
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    private static final LocalDateTime TEN = LocalDateTime.of(2030, 3, 1, 10, 0);
    private static final LocalDateTime NOON = LocalDateTime.of(2030, 3, 1, 12, 0);
    private static final long NO_BOOKING = -1L;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private Room room;
    private User student;
    private Booking existing;

    @BeforeEach
    void setUp() {
        room = roomRepository.findAll(keyword("SC09-9226")).get(0);
        student = userRepository.findByEmail("student@kkumail.com").orElseThrow();
        existing = bookingRepository.save(booking(TEN, NOON, BookingStatus.APPROVED));
    }

    @Test
    void existsOverlap_coversWholeExistingRange_isConflict() {
        assertThat(overlaps(TEN.minusHours(1), NOON.plusHours(1))).isTrue();
    }

    @Test
    void existsOverlap_partialOverlap_isConflict() {
        assertThat(overlaps(TEN.plusHours(1), NOON.plusHours(1))).isTrue();
        assertThat(overlaps(TEN.minusHours(1), TEN.plusMinutes(1))).isTrue();
    }

    @Test
    void existsOverlap_insideExistingRange_isConflict() {
        assertThat(overlaps(TEN.plusMinutes(30), TEN.plusMinutes(90))).isTrue();
    }

    @Test
    void existsOverlap_adjacentBookings_isNotConflict() {
        assertThat(overlaps(NOON, NOON.plusHours(1))).isFalse();
        assertThat(overlaps(TEN.minusHours(1), TEN)).isFalse();
    }

    @Test
    void existsOverlap_cancelledBooking_isIgnored() {
        existing.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(existing);

        assertThat(overlaps(TEN, NOON)).isFalse();
    }

    @Test
    void existsOverlap_excludesBookingBeingUpdated() {
        assertThat(bookingRepository.existsOverlap(room.getId(), TEN, NOON, BookingStatus.ACTIVE, existing.getId()))
                .isFalse();
    }

    @Test
    void findBookedRoomIds_returnsRoomOfActiveBooking() {
        assertThat(bookingRepository.findBookedRoomIds(TEN, NOON, BookingStatus.ACTIVE)).containsExactly(room.getId());
    }

    @Test
    void countActive_countsOnlyActiveFutureBookings() {
        long before = bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(student.getId(), BookingStatus.ACTIVE,
                TEN.minusDays(1));
        bookingRepository.save(booking(TEN.plusHours(5), NOON.plusHours(5), BookingStatus.REJECTED));

        assertThat(bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(student.getId(), BookingStatus.ACTIVE,
                TEN.minusDays(1))).isEqualTo(before);
    }

    private boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.existsOverlap(room.getId(), start, end, BookingStatus.ACTIVE, NO_BOOKING);
    }

    private Booking booking(LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return Booking.builder().user(student).room(room).startTime(start).endTime(end)
                .purpose("ทดสอบ").attendees(5).status(status).build();
    }
}
