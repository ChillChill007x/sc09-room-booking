package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.service.BookingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    /** ไม่มีการจองที่ต้องยกเว้น (existsOverlap ใช้ excludeId ตอนแก้ไขการจอง) */
    private static final long NO_BOOKING = -1L;

    private final BookingRepository bookingRepository;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findBookedRoomIds(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findBookedRoomIds(start, end, BookingStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFutureBookings(Long roomId) {
        return bookingRepository.existsByRoomIdAndStatusInAndEndTimeAfter(roomId, BookingStatus.ACTIVE,
                LocalDateTime.now(clock));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveBookingOverlap(Long roomId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.existsOverlap(roomId, start, end, BookingStatus.ACTIVE, NO_BOOKING);
    }
}
