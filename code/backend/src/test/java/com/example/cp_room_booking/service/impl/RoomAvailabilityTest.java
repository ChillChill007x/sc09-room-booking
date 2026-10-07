package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomClosure;
import com.example.cp_room_booking.dto.response.RoomResponse;
import com.example.cp_room_booking.mapper.RoomMapper;
import com.example.cp_room_booking.mapper.RoomTypeMapper;
import com.example.cp_room_booking.repository.RoomClosureRepository;
import com.example.cp_room_booking.repository.RoomRepository;
import com.example.cp_room_booking.service.BookingQueryService;
import com.example.cp_room_booking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ค้นหาห้องว่างกับฐานข้อมูลจริง โดย mock BookingQueryService ของคนที่ 3
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RoomServiceImpl.class, RoomMapper.class, RoomTypeMapper.class})
class RoomAvailabilityTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 1, 15, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 1, 15, 11, 0);

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomClosureRepository roomClosureRepository;

    @MockitoBean
    private BookingQueryService bookingQueryService;

    @Test
    void findAvailable_excludesBookedRoomsAndMaintenance() {
        Long booked = idOf("SC09-2201");
        when(bookingQueryService.findBookedRoomIds(any(), any())).thenReturn(Set.of(booked));

        List<RoomResponse> rooms = roomService.findAvailable(START, END, 40, null);

        assertThat(rooms).extracting(RoomResponse::code)
                .contains("SC09-2202", "SC09-1101")
                .doesNotContain("SC09-2201", "SC09-4403", "SC09-1103");
    }

    @Test
    void findAvailable_excludesClosedRooms() {
        Room room = roomRepository.findById(idOf("SC09-3303")).orElseThrow();
        roomClosureRepository.save(RoomClosure.builder()
                .room(room).startTime(START.minusHours(1)).endTime(START.plusMinutes(30)).reason("ซ่อมแอร์").build());
        when(bookingQueryService.findBookedRoomIds(any(), any())).thenReturn(Set.of());

        List<RoomResponse> rooms = roomService.findAvailable(START, END, null, null);

        assertThat(rooms).extracting(RoomResponse::code).doesNotContain("SC09-3303").contains("SC09-4401");
    }

    @Test
    void findAvailable_requiresAllSelectedEquipment() {
        when(bookingQueryService.findBookedRoomIds(any(), any())).thenReturn(Set.of());
        Room lab = roomRepository.findDetailById(idOf("SC09-2201")).orElseThrow();
        List<Long> equipmentIds = lab.getEquipment().stream().map(link -> link.getEquipment().getId()).toList();

        List<RoomResponse> rooms = roomService.findAvailable(START, END, null, equipmentIds);

        assertThat(rooms).extracting(RoomResponse::code).containsExactlyInAnyOrder("SC09-2201", "SC09-2202");
    }

    private Long idOf(String code) {
        return roomRepository.findAll(keyword(code)).get(0).getId();
    }
}
