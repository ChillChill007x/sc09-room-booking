package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Equipment;
import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomType;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.dto.request.RoomEquipmentRequest;
import com.example.cp_room_booking.dto.request.RoomRequest;
import com.example.cp_room_booking.dto.response.RoomResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.RoomMapper;
import com.example.cp_room_booking.mapper.RoomTypeMapper;
import com.example.cp_room_booking.repository.EquipmentRepository;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.repository.RoomTypeRepository;
import com.example.cp_room_booking.service.BookingQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private RoomClosureRepository roomClosureRepository;
    @Mock
    private BookingQueryService bookingQueryService;

    private RoomServiceImpl roomService;
    private final RoomType lectureType = RoomType.builder().id(1L).name("ห้องบรรยาย").build();

    @BeforeEach
    void setUp() {
        roomService = new RoomServiceImpl(roomRepository, roomTypeRepository, equipmentRepository,
                roomClosureRepository, bookingQueryService, new RoomMapper(new RoomTypeMapper()));
    }

    @Test
    void create_newCode_savesRoomAsActiveWithUpperCaseCode() {
        when(roomRepository.existsByCodeIgnoreCase("sc09-9999")).thenReturn(false);
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(lectureType));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse response = roomService.create(request("sc09-9999"));

        assertThat(response.code()).isEqualTo("SC09-9999");
        assertThat(response.status()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(response.roomType().name()).isEqualTo("ห้องบรรยาย");
    }

    @Test
    void create_duplicateCode_throwsConflict() {
        when(roomRepository.existsByCodeIgnoreCase("SC09-1101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.create(request("SC09-1101"))).isInstanceOf(ConflictException.class);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void update_codeUsedByAnotherRoom_throwsConflict() {
        when(roomRepository.findDetailById(5L)).thenReturn(Optional.of(room(5L)));
        when(roomRepository.existsByCodeIgnoreCaseAndIdNot("SC09-1101", 5L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.update(5L, request("SC09-1101"))).isInstanceOf(ConflictException.class);
    }

    @Test
    void delete_roomWithFutureBookings_throwsConflict() {
        Room room = room(5L);
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(bookingQueryService.hasFutureBookings(5L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.delete(5L)).isInstanceOf(ConflictException.class);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.ACTIVE);
    }

    @Test
    void delete_roomWithoutFutureBookings_setsInactive() {
        Room room = room(5L);
        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(bookingQueryService.hasFutureBookings(5L)).thenReturn(false);

        roomService.delete(5L);

        assertThat(room.getStatus()).isEqualTo(RoomStatus.INACTIVE);
    }

    @Test
    void getById_missingRoom_throwsNotFound() {
        when(roomRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEquipment_unknownEquipment_throwsNotFound() {
        when(roomRepository.findDetailById(5L)).thenReturn(Optional.of(room(5L)));
        when(equipmentRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> roomService.updateEquipment(5L, List.of(new RoomEquipmentRequest(42L, 1))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEquipment_duplicateEquipment_throwsBusinessRule() {
        when(roomRepository.findDetailById(5L)).thenReturn(Optional.of(room(5L)));

        assertThatThrownBy(() -> roomService.updateEquipment(5L, List.of(
                new RoomEquipmentRequest(1L, 1), new RoomEquipmentRequest(1L, 2))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateEquipment_setsQuantities() {
        Room room = room(5L);
        Equipment projector = Equipment.builder().id(1L).name("โปรเจกเตอร์").build();
        when(roomRepository.findDetailById(5L)).thenReturn(Optional.of(room));
        when(equipmentRepository.findAllById(any())).thenReturn(List.of(projector));

        RoomResponse response = roomService.updateEquipment(5L, List.of(new RoomEquipmentRequest(1L, 3)));

        assertThat(response.equipment()).singleElement()
                .satisfies(item -> assertThat(item.quantity()).isEqualTo(3));
    }

    @Test
    void findAvailable_endBeforeStart_throwsBusinessRuleWithoutQuerying() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 12, 0);

        assertThatThrownBy(() -> roomService.findAvailable(start, start.minusHours(1), null, null))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(bookingQueryService);
    }

    private RoomRequest request(String code) {
        return new RoomRequest(code, "ห้องทดสอบ", 1, 30, null, 1L, null);
    }

    private Room room(Long id) {
        return Room.builder()
                .id(id)
                .code("SC09-0005")
                .name("ห้อง 5")
                .floor(1)
                .capacity(30)
                .roomType(lectureType)
                .status(RoomStatus.ACTIVE)
                .build();
    }
}
