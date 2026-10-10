package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomClosure;
import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.dto.request.RoomClosureRequest;
import com.example.cp_room_booking.dto.response.RoomClosureResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.RoomMapper;
import com.example.cp_room_booking.mapper.RoomTypeMapper;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.service.BookingQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomClosureServiceImplTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 1, 15, 12, 0);

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomClosureRepository roomClosureRepository;
    @Mock
    private BookingQueryService bookingQueryService;

    private RoomClosureServiceImpl closureService;
    private final Room room = Room.builder().id(3L).code("SC09-3303").name("ประชุม").floor(3).capacity(20)
            .roomType(RoomType.builder().id(1L).name("ห้องประชุม").build()).status(RoomStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        closureService = new RoomClosureServiceImpl(roomRepository, roomClosureRepository,
                new RoomMapper(new RoomTypeMapper()), bookingQueryService);
    }

    @Test
    void create_overActiveBooking_throwsConflictAndDoesNotSave() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(roomClosureRepository.existsOverlap(3L, START, END)).thenReturn(false);
        when(bookingQueryService.hasActiveBookingOverlap(3L, START, END)).thenReturn(true);

        assertThatThrownBy(() -> closureService.create(3L, new RoomClosureRequest(START, END, "ซ่อม")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("มีการจองที่ยังใช้งานอยู่");
        verify(roomClosureRepository, never()).save(any());
    }

    @Test
    void create_locksRoomBeforeChecking() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(roomClosureRepository.save(any(RoomClosure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        closureService.create(3L, new RoomClosureRequest(START, END, "ซ่อม"));

        InOrder order = inOrder(roomRepository, bookingQueryService);
        order.verify(roomRepository).lockById(3L);
        order.verify(bookingQueryService).hasActiveBookingOverlap(3L, START, END);
    }

    @Test
    void create_overlappingClosure_throwsConflict() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(roomClosureRepository.existsOverlap(3L, START, END)).thenReturn(true);

        assertThatThrownBy(() -> closureService.create(3L, new RoomClosureRequest(START, END, "ซ่อม")))
                .isInstanceOf(ConflictException.class);
        verify(roomClosureRepository, never()).save(any());
    }

    @Test
    void create_endBeforeStart_throwsBusinessRule() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> closureService.create(3L, new RoomClosureRequest(END, START, "ซ่อม")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void create_freeSlot_savesClosure() {
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(roomClosureRepository.existsOverlap(3L, START, END)).thenReturn(false);
        when(roomClosureRepository.save(any(RoomClosure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomClosureResponse response = closureService.create(3L, new RoomClosureRequest(START, END, " ซ่อมแอร์ "));

        assertThat(response.roomId()).isEqualTo(3L);
        assertThat(response.reason()).isEqualTo("ซ่อมแอร์");
    }

    @Test
    void delete_closureOfAnotherRoom_throwsNotFound() {
        when(roomClosureRepository.findByIdAndRoomId(7L, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> closureService.delete(3L, 7L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
