package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.event.BookingCreatedEvent;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.mapper.BookingMapper;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.service.RoomQueryService;
import com.example.cp_room_booking.service.policy.BookingPolicy;
import com.example.cp_room_booking.service.policy.LecturerBookingPolicy;
import com.example.cp_room_booking.service.policy.StudentBookingPolicy;
import com.example.cp_room_booking.service.validation.BookingValidationChain;
import com.example.cp_room_booking.service.validation.BookingValidationContext;
import com.example.cp_room_booking.support.SecurityTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 14, 8, 0);
    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 9, 0);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomQueryService roomQueryService;
    @Mock
    private BookingValidationChain validationChain;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BookingServiceImpl bookingService;
    private final Room room = Room.builder().id(7L).code("SC09-2201").name("Lab").capacity(60)
            .status(RoomStatus.ACTIVE).build();
    private final User student = user(4L, Role.STUDENT);

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, roomQueryService, validationChain,
                new BookingMapper(), eventPublisher, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
    }

    @Test
    void create_studentBooking_isPendingWithHistoryAndPublishesEvent() {
        stubChain(new StudentBookingPolicy());
        when(userRepository.getReferenceById(4L)).thenReturn(student);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(100L);
            return booking;
        });

        BookingResponse response = bookingService.create(SecurityTestUtils.principal(4L, Role.STUDENT), request());

        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(saved.capture());
        assertThat(saved.getValue().getHistory()).singleElement().satisfies(history -> {
            assertThat(history.getFromStatus()).isNull();
            assertThat(history.getToStatus()).isEqualTo(BookingStatus.PENDING);
        });
        ArgumentCaptor<BookingCreatedEvent> event = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().bookingId()).isEqualTo(100L);
        assertThat(event.getValue().roomCode()).isEqualTo("SC09-2201");
    }

    @Test
    void create_lecturerBooking_isApprovedAutomatically() {
        stubChain(new LecturerBookingPolicy());
        when(userRepository.getReferenceById(3L)).thenReturn(user(3L, Role.LECTURER));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.create(SecurityTestUtils.principal(3L, Role.LECTURER), request());

        assertThat(response.status()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void create_chainRejects_doesNotSaveOrPublish() {
        doAnswer(invocation -> {
            throw new ConflictException("ชน");
        }).when(validationChain).validate(any());

        assertThatThrownBy(() -> bookingService.create(SecurityTestUtils.principal(4L, Role.STUDENT), request()))
                .isInstanceOf(ConflictException.class);
        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getById_otherUsersBooking_throwsForbidden() {
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking(BookingStatus.PENDING)));

        assertThatThrownBy(() -> bookingService.getById(100L, SecurityTestUtils.principal(5L, Role.STUDENT)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getById_staffCanSeeAnyBooking() {
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking(BookingStatus.PENDING)));

        BookingResponse response = bookingService.getById(100L, SecurityTestUtils.principal(2L, Role.STAFF));

        assertThat(response.userId()).isEqualTo(4L);
    }

    @Test
    void update_approvedBooking_throwsConflict() {
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking(BookingStatus.APPROVED)));

        assertThatThrownBy(() -> bookingService.update(100L, SecurityTestUtils.principal(4L, Role.STUDENT), request()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_notOwner_throwsForbidden() {
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking(BookingStatus.PENDING)));

        assertThatThrownBy(() -> bookingService.update(100L, SecurityTestUtils.principal(5L, Role.STUDENT), request()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void delete_ownPendingBooking_deletes() {
        Booking booking = booking(BookingStatus.PENDING);
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking));

        bookingService.delete(100L, SecurityTestUtils.principal(4L, Role.STUDENT));

        verify(bookingRepository).delete(booking);
    }

    @Test
    void findByUser_otherStudent_throwsForbidden() {
        assertThatThrownBy(() -> bookingService.findByUser(4L, SecurityTestUtils.principal(5L, Role.STUDENT), null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private void stubChain(BookingPolicy policy) {
        doAnswer(invocation -> {
            BookingValidationContext context = invocation.getArgument(0);
            context.setPolicy(policy);
            context.setRoom(room);
            return null;
        }).when(validationChain).validate(any());
    }

    private BookingRequest request() {
        return new BookingRequest(7L, START, START.plusHours(2), "ติวหนังสือ", 10);
    }

    private Booking booking(BookingStatus status) {
        return Booking.builder().id(100L).user(student).room(room).startTime(START).endTime(START.plusHours(2))
                .purpose("ติว").attendees(10).status(status).build();
    }

    private static User user(Long id, Role role) {
        return User.builder().id(id).email("u" + id + "@kkumail.com").passwordHash("x").role(role)
                .status(UserStatus.ACTIVE).build();
    }
}
