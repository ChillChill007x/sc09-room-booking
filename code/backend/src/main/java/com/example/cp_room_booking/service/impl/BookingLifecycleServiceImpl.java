package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.response.AllowedActionsResponse;
import com.example.cp_room_booking.dto.response.BookingHistoryResponse;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.event.BookingStatusChangedEvent;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.exception.InvalidBookingStateException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.BookingHistoryMapper;
import com.example.cp_room_booking.mapper.BookingMapper;
import com.example.cp_room_booking.repository.BookingLifecycleRepository;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.BookingStatusHistoryRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingLifecycleService;
import com.example.cp_room_booking.service.state.BookingState;
import com.example.cp_room_booking.service.state.BookingStateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * ทุกการเปลี่ยนสถานะหลังสร้างการจอง ตรวจตามลำดับ: สิทธิ์ (403) → state รับ action ได้ไหม (409) → กฎเวลา (400)
 * แล้วบันทึก history และ publish BookingStatusChangedEvent
 */
@Service
@RequiredArgsConstructor
public class BookingLifecycleServiceImpl implements BookingLifecycleService {

    private static final Duration CHECK_IN_WINDOW = Duration.ofMinutes(15);
    private static final String NO_SHOW_NOTE = "ไม่ check-in ภายใน 15 นาทีหลังเวลาเริ่ม";
    private static final String AUTO_COMPLETE_NOTE = "ระบบปิดการจองเมื่อเลยเวลาสิ้นสุด";

    private final BookingRepository bookingRepository;
    private final BookingLifecycleRepository lifecycleRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final BookingStateFactory stateFactory;
    private final BookingMapper bookingMapper;
    private final BookingHistoryMapper historyMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    @Transactional
    public BookingResponse changeStatus(Long bookingId, BookingAction action, String note, UserPrincipal actor) {
        Booking booking = find(bookingId);
        if (!isPermitted(action, booking, actor)) {
            throw new ForbiddenOperationException("ไม่มีสิทธิ์ทำรายการ " + action + " กับการจองนี้");
        }
        BookingState state = stateFactory.of(booking.getStatus());
        if (!state.canHandle(action)) {
            throw new InvalidBookingStateException(booking.getStatus(), action);
        }
        if (action == BookingAction.REJECT && (note == null || note.isBlank())) {
            throw new BusinessRuleException("กรุณาระบุเหตุผลที่ปฏิเสธ");
        }
        LocalDateTime now = now();
        timeViolation(action, booking, now).ifPresent(message -> {
            throw new BusinessRuleException(message);
        });
        User changedBy = userRepository.getReferenceById(actor.getId());
        transition(booking, state, action, changedBy, note, now);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryResponse> getHistory(Long bookingId, UserPrincipal actor) {
        Booking booking = find(bookingId);
        if (!actor.isStaff() && !booking.isOwnedBy(actor.getId())) {
            throw new ForbiddenOperationException("ดูประวัติได้เฉพาะการจองของตัวเอง");
        }
        return historyRepository.findByBookingIdOrderByChangedAtAscIdAsc(bookingId).stream()
                .map(historyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AllowedActionsResponse getAllowedActions(Long bookingId, UserPrincipal actor) {
        Booking booking = find(bookingId);
        if (!actor.isStaff() && !booking.isOwnedBy(actor.getId())) {
            throw new ForbiddenOperationException("ดูได้เฉพาะการจองของตัวเอง");
        }
        BookingState state = stateFactory.of(booking.getStatus());
        LocalDateTime now = now();
        List<BookingAction> actions = Arrays.stream(BookingAction.values())
                .filter(state::canHandle)
                .filter(action -> isPermitted(action, booking, actor))
                .filter(action -> timeViolation(action, booking, now).isEmpty())
                .toList();
        return AllowedActionsResponse.builder()
                .bookingId(bookingId)
                .status(booking.getStatus())
                .actions(actions)
                .build();
    }

    @Override
    @Transactional
    public int markNoShows() {
        LocalDateTime now = now();
        List<Booking> overdue = lifecycleRepository.findByStatusAndStartTimeBefore(
                BookingStatus.APPROVED, now.minus(CHECK_IN_WINDOW));
        BookingState state = stateFactory.of(BookingStatus.APPROVED);
        overdue.forEach(booking -> transition(booking, state, BookingAction.MARK_NO_SHOW, null, NO_SHOW_NOTE, now));
        return overdue.size();
    }

    @Override
    @Transactional
    public int completeFinished() {
        LocalDateTime now = now();
        List<Booking> finished = lifecycleRepository.findByStatusAndEndTimeBefore(BookingStatus.CHECKED_IN, now);
        BookingState state = stateFactory.of(BookingStatus.CHECKED_IN);
        finished.forEach(booking -> transition(booking, state, BookingAction.COMPLETE, null, AUTO_COMPLETE_NOTE, now));
        return finished.size();
    }

    private void transition(Booking booking, BookingState state, BookingAction action, User changedBy, String note,
                            LocalDateTime now) {
        BookingStatus from = booking.getStatus();
        BookingStatus to = state.next(action).orElseThrow(() -> new InvalidBookingStateException(from, action));
        booking.changeStatus(to, changedBy, note, now);
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking.getId(), booking.getUser().getId(),
                from, to, note, booking.getRoom().getCode(), booking.getStartTime()));
    }

    /**
     * อนุมัติ ปฏิเสธ และตั้ง NO_SHOW ได้เฉพาะเจ้าหน้าที่ check-in ได้เฉพาะเจ้าของ
     * ยกเลิกและจบการใช้งานได้ทั้งเจ้าของและเจ้าหน้าที่
     */
    private boolean isPermitted(BookingAction action, Booking booking, UserPrincipal actor) {
        boolean owner = booking.isOwnedBy(actor.getId());
        return switch (action) {
            case APPROVE, REJECT, MARK_NO_SHOW -> actor.isStaff();
            case CHECK_IN -> owner;
            case CANCEL, COMPLETE -> owner || actor.isStaff();
        };
    }

    private Optional<String> timeViolation(BookingAction action, Booking booking, LocalDateTime now) {
        LocalDateTime start = booking.getStartTime();
        return switch (action) {
            case APPROVE -> now.isBefore(start)
                    ? Optional.empty()
                    : Optional.of("การจองนี้เลยเวลาเริ่มแล้ว อนุมัติไม่ได้");
            case CANCEL -> now.isBefore(start)
                    ? Optional.empty()
                    : Optional.of("ยกเลิกได้ก่อนเวลาเริ่มเท่านั้น");
            case CHECK_IN -> !now.isBefore(start.minus(CHECK_IN_WINDOW)) && !now.isAfter(start.plus(CHECK_IN_WINDOW))
                    ? Optional.empty()
                    : Optional.of("check-in ได้ตั้งแต่ 15 นาทีก่อนถึง 15 นาทีหลังเวลาเริ่ม");
            case MARK_NO_SHOW -> now.isAfter(start.plus(CHECK_IN_WINDOW))
                    ? Optional.empty()
                    : Optional.of("ตั้งเป็นไม่มาใช้ห้องได้หลังเวลาเริ่ม 15 นาที");
            case REJECT, COMPLETE -> Optional.empty();
        };
    }

    private Booking find(Long bookingId) {
        return bookingRepository.findDetailById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("การจอง", bookingId));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
