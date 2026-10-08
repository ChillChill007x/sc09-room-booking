package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.service.RoomQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosureHandlerTest {

    @Mock
    private RoomQueryService roomQueryService;

    @InjectMocks
    private ClosureHandler handler;

    @Test
    void roomClosedInRange_throwsConflict() {
        when(roomQueryService.isClosed(eq(1L), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> handler.check(TimeRangeHandlerTest.context("2030-01-15T09:00", "2030-01-15T10:00")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void roomOpen_passes() {
        when(roomQueryService.isClosed(eq(1L), any(), any())).thenReturn(false);

        assertThatCode(() -> handler.check(TimeRangeHandlerTest.context("2030-01-15T09:00", "2030-01-15T10:00")))
                .doesNotThrowAnyException();
    }
}
