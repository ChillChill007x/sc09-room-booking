package com.example.cp_room_booking.service;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.dto.response.BookingSlotResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse create(UserPrincipal actor, BookingRequest request);

    BookingResponse getById(Long id, UserPrincipal actor);

    BookingResponse update(Long id, UserPrincipal actor, BookingRequest request);

    void delete(Long id, UserPrincipal actor);

    PageResponse<BookingResponse> findAll(BookingStatus status, Long roomId, LocalDate from, LocalDate to,
                                          Pageable pageable);

    PageResponse<BookingResponse> findByUser(Long userId, UserPrincipal actor, Pageable pageable);

    List<BookingSlotResponse> findRoomSchedule(Long roomId, LocalDate date);
}
