package com.example.cp_room_booking.repository;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.entity.RoomClosure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static com.example.cp_room_booking.repository.specification.RoomSpecifications.keyword;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ตรวจเงื่อนไขช่วงปิดห้องทับกัน: ทับบางส่วน ครอบทั้งช่วง และต่อกันพอดี
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomClosureRepositoryTest {

    private static final LocalDateTime START = LocalDateTime.of(2030, 2, 1, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2030, 2, 1, 12, 0);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomClosureRepository roomClosureRepository;

    private Long roomId;

    @BeforeEach
    void setUp() {
        Room room = roomRepository.findAll(keyword("SC09-9226")).get(0);
        roomId = room.getId();
        roomClosureRepository.save(RoomClosure.builder().room(room).startTime(START).endTime(END).reason("ซ่อม").build());
    }

    @Test
    void existsOverlap_partialOverlap_returnsTrue() {
        assertThat(roomClosureRepository.existsOverlap(roomId, START.plusHours(1), END.plusHours(1))).isTrue();
    }

    @Test
    void existsOverlap_containsWholeRange_returnsTrue() {
        assertThat(roomClosureRepository.existsOverlap(roomId, START.minusHours(1), END.plusHours(1))).isTrue();
    }

    @Test
    void existsOverlap_adjacentRanges_returnsFalse() {
        assertThat(roomClosureRepository.existsOverlap(roomId, END, END.plusHours(1))).isFalse();
        assertThat(roomClosureRepository.existsOverlap(roomId, START.minusHours(1), START)).isFalse();
    }

    @Test
    void findClosedRoomIds_returnsRoomInRange() {
        assertThat(roomClosureRepository.findClosedRoomIds(START, END)).contains(roomId);
    }
}
