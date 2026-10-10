package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.response.AllowedActionsResponse;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.event.BookingStatusChangedEvent;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.exception.InvalidBookingStateException;
import com.example.cp_room_booking.mapper.BookingHistoryMapper;
import com.example.cp_room_booking.mapper.BookingMapper;
import com.example.cp_room_booking.repository.BookingLifecycleRepository;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.BookingStatusHistoryRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.RoomQueryService;
import com.example.cp_room_booking.service.state.ApprovedState;
import com.example.cp_room_booking.service.state.BookingStateFactory;
import com.example.cp_room_booking.service.state.CancelledState;
import com.example.cp_room_booking.service.state.CheckedInState;
import com.example.cp_room_booking.service.state.CompletedState;
import com.example.cp_room_booking.service.state.NoShowState;
import com.example.cp_room_booking.service.state.PendingState;
import com.example.cp_room_booking.service.state.RejectedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.example.cp_room_booking.support.SecurityTestUtils.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingLifecycleServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");
    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 10, 0);
    private static final long OWNER_ID = 4L;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingLifecycleRepository lifecycleRepository;
    @Mock
    private BookingStatusHistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RoomQueryService roomQueryService;

    private final User owner = User.builder().id(OWNER_ID).email("student@kkumail.com").passwordHash("x")
            .role(Role.STUDENT).status(UserStatus.ACTIVE).build();
    private final Room room = Room.builder().id(7L).code("SC09-2201").name("Lab").capacity(60)
            .status(RoomStatus.ACTIVE).build();
    private final BookingStateFactory factory = new BookingStateFactory(List.of(
            new PendingState(), new ApprovedState(), new CheckedInState(), new RejectedState(),
            new CancelledState(), new CompletedState(), new NoShowState()));

    @BeforeEach
    void setUp() {
        when(userRepository.getReferenceById(anyLong()))
                .thenAnswer(invocation -> User.builder().id(invocation.getArgument(0)).build());
    }

    @Test
    void approve_byStaff_changesStatusRecordsHistoryAndPublishesEvent() {
        Booking booking = stub(BookingStatus.PENDING);

        BookingResponse response = service(START.minusDays(1))
                .changeStatus(100L, BookingAction.APPROVE, null, principal(2L, Role.STAFF));

        assertThat(response.status()).isEqualTo(BookingStatus.APPROVED);
        assertThat(booking.getHistory()).singleElement().satisfies(history -> {
            assertThat(history.getFromStatus()).isEqualTo(BookingStatus.PENDING);
            assertThat(history.getToStatus()).isEqualTo(BookingStatus.APPROVED);
            assertThat(history.getChangedBy().getId()).isEqualTo(2L);
        });
        ArgumentCaptor<BookingStatusChangedEvent> event = ArgumentCaptor.forClass(BookingStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().userId()).isEqualTo(OWNER_ID);
        assertThat(event.getValue().toStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @ParameterizedTest(name = "{0} โดย {1} ได้ 403")
    @CsvSource({
            "APPROVE, STUDENT",
            "APPROVE, LECTURER",
            "REJECT, STUDENT",
            "MARK_NO_SHOW, LECTURER"
    })
    void staffOnlyActions_byNonStaff_areForbidden(BookingAction action, Role role) {
        stub(BookingStatus.PENDING);

        assertThatThrownBy(() -> service(START.minusDays(1)).changeStatus(100L, action, "x", principal(OWNER_ID, role)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkIn_byStaff_isForbidden() {
        stub(BookingStatus.APPROVED);

        assertThatThrownBy(() -> service(START).changeStatus(100L, BookingAction.CHECK_IN, null,
                principal(2L, Role.STAFF))).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void cancel_byOtherStudent_isForbidden() {
        stub(BookingStatus.PENDING);

        assertThatThrownBy(() -> service(START.minusDays(1)).changeStatus(100L, BookingAction.CANCEL, null,
                principal(9L, Role.STUDENT))).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void approve_alreadyRejected_throwsInvalidState409() {
        stub(BookingStatus.REJECTED);

        assertThatThrownBy(() -> service(START.minusDays(1)).changeStatus(100L, BookingAction.APPROVE, null,
                principal(2L, Role.STAFF))).isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    void reject_withoutNote_throwsBusinessRule() {
        stub(BookingStatus.PENDING);

        assertThatThrownBy(() -> service(START.minusDays(1)).changeStatus(100L, BookingAction.REJECT, " ",
                principal(2L, Role.STAFF))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancel_afterStart_throwsBusinessRule() {
        stub(BookingStatus.APPROVED);

        assertThatThrownBy(() -> service(START.plusMinutes(1)).changeStatus(100L, BookingAction.CANCEL, null,
                principal(OWNER_ID, Role.STUDENT))).isInstanceOf(BusinessRuleException.class);
    }

    @ParameterizedTest(name = "check-in ตอน {0} นาทีจากเวลาเริ่ม ผ่าน = {1}")
    @CsvSource({
            "-16, false",
            "-15, true",
            "0, true",
            "15, true",
            "16, false"
    })
    void checkIn_onlyWithinFifteenMinutesWindow(long minutesFromStart, boolean allowed) {
        stub(BookingStatus.APPROVED);
        BookingLifecycleServiceImpl service = service(START.plusMinutes(minutesFromStart));
        UserPrincipal actor = principal(OWNER_ID, Role.STUDENT);

        if (allowed) {
            assertThat(service.changeStatus(100L, BookingAction.CHECK_IN, null, actor).status())
                    .isEqualTo(BookingStatus.CHECKED_IN);
        } else {
            assertThatThrownBy(() -> service.changeStatus(100L, BookingAction.CHECK_IN, null, actor))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    void allowedActions_ownerOfApprovedBookingAtStart_canOnlyCheckIn() {
        stub(BookingStatus.APPROVED);

        AllowedActionsResponse allowed = service(START.plusMinutes(5)).getAllowedActions(100L,
                principal(OWNER_ID, Role.STUDENT));

        assertThat(allowed.actions()).containsExactly(BookingAction.CHECK_IN);
    }

    @Test
    void allowedActions_staffOnPendingBooking_canApproveRejectCancel() {
        stub(BookingStatus.PENDING);

        AllowedActionsResponse allowed = service(START.minusDays(1)).getAllowedActions(100L, principal(2L, Role.STAFF));

        assertThat(allowed.actions()).containsExactlyInAnyOrder(
                BookingAction.APPROVE, BookingAction.REJECT, BookingAction.CANCEL);
    }

    @Test
    void history_ofOtherUsersBooking_isForbidden() {
        stub(BookingStatus.PENDING);

        assertThatThrownBy(() -> service(START).getHistory(100L, principal(9L, Role.STUDENT)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void markNoShows_changesOverdueApprovedBookingsWithoutActor() {
        Booking overdue = booking(BookingStatus.APPROVED);
        when(lifecycleRepository.findByStatusAndStartTimeBefore(any(), any())).thenReturn(List.of(overdue));
        when(lifecycleRepository.findStatusById(100L)).thenReturn(Optional.of(BookingStatus.APPROVED));

        int changed = service(START.plusMinutes(20)).markNoShows();

        assertThat(changed).isEqualTo(1);
        assertThat(overdue.getStatus()).isEqualTo(BookingStatus.NO_SHOW);
        assertThat(overdue.getHistory().get(0).getChangedBy()).isNull();
        verify(lifecycleRepository).findByStatusAndStartTimeBefore(BookingStatus.APPROVED, START.plusMinutes(5));
    }

    @Test
    void completeFinished_changesCheckedInBookingsPastEndTime() {
        Booking finished = booking(BookingStatus.CHECKED_IN);
        when(lifecycleRepository.findByStatusAndEndTimeBefore(any(), any())).thenReturn(List.of(finished));
        when(lifecycleRepository.findStatusById(100L)).thenReturn(Optional.of(BookingStatus.CHECKED_IN));

        int changed = service(START.plusHours(3)).completeFinished();

        assertThat(changed).isEqualTo(1);
        assertThat(finished.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void markNoShows_skipsBookingCheckedInAfterSchedulerRead() {
        // scheduler อ่านได้ APPROVED แต่ก่อนล็อก เจ้าของ check-in ทันแล้ว
        Booking overdue = booking(BookingStatus.APPROVED);
        when(lifecycleRepository.findByStatusAndStartTimeBefore(any(), any())).thenReturn(List.of(overdue));
        when(lifecycleRepository.findStatusById(100L)).thenReturn(Optional.of(BookingStatus.CHECKED_IN));

        int changed = service(START.plusMinutes(20)).markNoShows();

        assertThat(changed).isZero();
        assertThat(overdue.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(lifecycleRepository).lockById(100L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void changeStatus_locksBookingBeforeReadingIt() {
        stub(BookingStatus.PENDING);

        service(START.minusDays(1)).changeStatus(100L, BookingAction.APPROVE, null, principal(2L, Role.STAFF));

        InOrder order = inOrder(lifecycleRepository, bookingRepository);
        order.verify(lifecycleRepository).lockById(100L);
        order.verify(bookingRepository).findDetailById(100L);
    }

    @Test
    void approve_whenRoomClosedDuringBooking_throwsConflictAndKeepsPending() {
        Booking booking = stub(BookingStatus.PENDING);
        when(roomQueryService.isClosed(7L, START, START.plusHours(2))).thenReturn(true);

        assertThatThrownBy(() -> service(START.minusDays(1))
                .changeStatus(100L, BookingAction.APPROVE, null, principal(2L, Role.STAFF)))
                .isInstanceOf(ConflictException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkIn_whenRoomClosedDuringBooking_throwsConflict() {
        stub(BookingStatus.APPROVED);
        when(roomQueryService.isClosed(7L, START, START.plusHours(2))).thenReturn(true);

        assertThatThrownBy(() -> service(START).changeStatus(100L, BookingAction.CHECK_IN, null,
                principal(OWNER_ID, Role.STUDENT))).isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_whenRoomNoLongerActive_isRejected() {
        Booking booking = stub(BookingStatus.PENDING);
        when(roomQueryService.getActiveRoom(7L)).thenThrow(new BusinessRuleException("ห้องไม่เปิดให้จอง"));

        assertThatThrownBy(() -> service(START.minusDays(1))
                .changeStatus(100L, BookingAction.APPROVE, null, principal(2L, Role.STAFF)))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void reject_whenRoomClosed_isStillAllowed() {
        Booking booking = stub(BookingStatus.PENDING);
        when(roomQueryService.isClosed(any(), any(), any())).thenReturn(true);

        service(START.minusDays(1)).changeStatus(100L, BookingAction.REJECT, "ห้องปิดซ่อม", principal(2L, Role.STAFF));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    private BookingLifecycleServiceImpl service(LocalDateTime now) {
        Clock clock = Clock.fixed(now.atZone(ZONE).toInstant(), ZONE);
        return new BookingLifecycleServiceImpl(bookingRepository, lifecycleRepository, historyRepository,
                userRepository, roomQueryService, factory, new BookingMapper(), new BookingHistoryMapper(),
                eventPublisher, clock);
    }

    private Booking stub(BookingStatus status) {
        Booking booking = booking(status);
        when(bookingRepository.findDetailById(100L)).thenReturn(Optional.of(booking));
        return booking;
    }

    private Booking booking(BookingStatus status) {
        return Booking.builder().id(100L).user(owner).room(room).startTime(START).endTime(START.plusHours(2))
                .purpose("ติว").attendees(10).status(status).build();
    }
}
