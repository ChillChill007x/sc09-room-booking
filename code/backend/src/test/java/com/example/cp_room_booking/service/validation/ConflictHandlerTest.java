package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConflictHandlerTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ConflictHandler handler;

    @Test
    void overlappingBooking_throwsConflict() {
        when(bookingRepository.existsOverlap(eq(1L), any(), any(), any(), eq(-1L))).thenReturn(true);

        assertThatThrownBy(() -> handler.check(context(null))).isInstanceOf(ConflictException.class);
    }

    @Test
    void freeSlot_passes() {
        when(bookingRepository.existsOverlap(eq(1L), any(), any(), any(), eq(-1L))).thenReturn(false);

        assertThatCode(() -> handler.check(context(null))).doesNotThrowAnyException();
    }

    @Test
    void update_excludesTheBookingItself() {
        when(bookingRepository.existsOverlap(eq(1L), any(), any(), any(), eq(55L))).thenReturn(false);

        handler.check(context(55L));

        verify(bookingRepository).existsOverlap(eq(1L), any(), any(), any(), eq(55L));
    }

    private BookingValidationContext context(Long excludeId) {
        LocalDateTime start = LocalDateTime.of(2030, 1, 15, 9, 0);
        return BookingValidationContext.builder()
                .userId(1L).role(Role.STUDENT).roomId(1L)
                .startTime(start).endTime(start.plusHours(1)).attendees(3)
                .excludeBookingId(excludeId)
                .build();
    }
}
