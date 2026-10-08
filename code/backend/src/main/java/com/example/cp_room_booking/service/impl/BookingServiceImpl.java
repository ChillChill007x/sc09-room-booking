package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.dto.response.BookingSlotResponse;
import com.example.cp_room_booking.event.BookingCreatedEvent;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ForbiddenOperationException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.BookingMapper;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingService;
import com.example.cp_room_booking.service.RoomQueryService;
import com.example.cp_room_booking.service.validation.BookingValidationChain;
import com.example.cp_room_booking.service.validation.BookingValidationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.cp_room_booking.repository.specification.BookingSpecifications.hasRoom;
import static com.example.cp_room_booking.repository.specification.BookingSpecifications.hasStatus;
import static com.example.cp_room_booking.repository.specification.BookingSpecifications.startsAtOrAfter;
import static com.example.cp_room_booking.repository.specification.BookingSpecifications.startsBefore;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomQueryService roomQueryService;
    private final BookingValidationChain validationChain;
    private final BookingMapper bookingMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    /**
     * ตรวจกฎผ่าน chain แล้วกำหนดสถานะเริ่มต้นจาก policy ของ role
     * ต้องรออนุมัติเป็น PENDING ไม่ต้องรอเป็น APPROVED แล้ว publish event ให้โมดูลแจ้งเตือน
     */
    @Override
    @Transactional
    public BookingResponse create(UserPrincipal actor, BookingRequest request) {
        BookingValidationContext context = contextOf(actor, request, null);
        validationChain.validate(context);

        User user = userRepository.getReferenceById(actor.getId());
        Booking booking = bookingMapper.toEntity(request, user, context.getRoom());
        BookingStatus initial = context.getPolicy().requiresApproval() ? BookingStatus.PENDING : BookingStatus.APPROVED;
        booking.changeStatus(initial, user, "สร้างการจอง", LocalDateTime.now(clock));
        Booking saved = bookingRepository.save(booking);

        eventPublisher.publishEvent(new BookingCreatedEvent(saved.getId(), actor.getId(),
                saved.getRoom().getCode(), saved.getStartTime(), saved.getEndTime(), saved.getStatus()));
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id, UserPrincipal actor) {
        Booking booking = findDetail(id);
        if (!actor.isStaff() && !booking.isOwnedBy(actor.getId())) {
            throw new ForbiddenOperationException("ดูได้เฉพาะการจองของตัวเอง");
        }
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse update(Long id, UserPrincipal actor, BookingRequest request) {
        Booking booking = findEditable(id, actor);
        BookingValidationContext context = contextOf(actor, request, id);
        validationChain.validate(context);
        bookingMapper.updateEntity(booking, request, context.getRoom());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public void delete(Long id, UserPrincipal actor) {
        bookingRepository.delete(findEditable(id, actor));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> findAll(BookingStatus status, Long roomId, LocalDate from, LocalDate to,
                                                 Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessRuleException("วันที่เริ่มต้องไม่หลังวันที่สิ้นสุด");
        }
        List<Specification<Booking>> specs = new ArrayList<>();
        if (status != null) {
            specs.add(hasStatus(status));
        }
        if (roomId != null) {
            specs.add(hasRoom(roomId));
        }
        if (from != null) {
            specs.add(startsAtOrAfter(from.atStartOfDay()));
        }
        if (to != null) {
            specs.add(startsBefore(to.plusDays(1).atStartOfDay()));
        }
        return PageResponse.from(bookingRepository.findAll(Specification.allOf(specs), pageable)
                .map(bookingMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> findByUser(Long userId, UserPrincipal actor, Pageable pageable) {
        if (!actor.isStaff() && !actor.getId().equals(userId)) {
            throw new ForbiddenOperationException("ดูได้เฉพาะการจองของตัวเอง");
        }
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException.of("ผู้ใช้", userId);
        }
        return PageResponse.from(bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingSlotResponse> findRoomSchedule(Long roomId, LocalDate date) {
        if (!roomQueryService.existsById(roomId)) {
            throw ResourceNotFoundException.of("ห้อง", roomId);
        }
        return bookingRepository.findRoomSchedule(roomId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(),
                        BookingStatus.ACTIVE).stream()
                .map(bookingMapper::toSlot)
                .toList();
    }

    /**
     * แก้และลบได้เฉพาะเจ้าของ และเฉพาะตอนที่ยัง PENDING
     */
    private Booking findEditable(Long id, UserPrincipal actor) {
        Booking booking = findDetail(id);
        if (!booking.isOwnedBy(actor.getId())) {
            throw new ForbiddenOperationException("แก้ไขหรือลบได้เฉพาะการจองของตัวเอง");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("แก้ไขหรือลบได้เฉพาะการจองที่รออนุมัติ สถานะปัจจุบันคือ " + booking.getStatus());
        }
        return booking;
    }

    private Booking findDetail(Long id) {
        return bookingRepository.findDetailById(id).orElseThrow(() -> ResourceNotFoundException.of("การจอง", id));
    }

    private BookingValidationContext contextOf(UserPrincipal actor, BookingRequest request, Long excludeId) {
        return BookingValidationContext.builder()
                .userId(actor.getId())
                .role(actor.getRole())
                .roomId(request.roomId())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .attendees(request.attendees())
                .excludeBookingId(excludeId)
                .build();
    }
}
