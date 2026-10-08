package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.entity.Room;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.service.RoomQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomHandlerTest {

    @Mock
    private RoomQueryService roomQueryService;

    @InjectMocks
    private RoomHandler handler;

    private final Room room = Room.builder().id(7L).code("SC09-1103").capacity(8).status(RoomStatus.ACTIVE).build();

    @Test
    void attendeesWithinCapacity_setsRoomOnContext() {
        when(roomQueryService.getActiveRoom(7L)).thenReturn(room);
        BookingValidationContext context = context(8);

        handler.check(context);

        assertThat(context.getRoom()).isSameAs(room);
    }

    @Test
    void attendeesOverCapacity_throws() {
        when(roomQueryService.getActiveRoom(7L)).thenReturn(room);

        assertThatThrownBy(() -> handler.check(context(9)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("รับได้ 8 คน");
    }

    @Test
    void inactiveRoom_propagatesErrorFromRoomQueryService() {
        when(roomQueryService.getActiveRoom(7L)).thenThrow(new BusinessRuleException("ห้องไม่เปิดให้จอง"));

        assertThatThrownBy(() -> handler.check(context(1))).isInstanceOf(BusinessRuleException.class);
    }

    private BookingValidationContext context(int attendees) {
        LocalDateTime start = LocalDateTime.of(2030, 1, 15, 9, 0);
        return BookingValidationContext.builder()
                .userId(1L).role(Role.STUDENT).roomId(7L)
                .startTime(start).endTime(start.plusHours(1)).attendees(attendees)
                .build();
    }
}
