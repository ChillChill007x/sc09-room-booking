package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.service.policy.BookingPolicyResolver;
import com.example.cp_room_booking.service.policy.LecturerBookingPolicy;
import com.example.cp_room_booking.service.policy.StaffBookingPolicy;
import com.example.cp_room_booking.service.policy.StudentBookingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyHandlerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 14, 8, 0);

    @Mock
    private BookingRepository bookingRepository;

    private PolicyHandler handler;

    @BeforeEach
    void setUp() {
        BookingPolicyResolver resolver = new BookingPolicyResolver(List.of(
                new StudentBookingPolicy(), new LecturerBookingPolicy(), new StaffBookingPolicy()));
        handler = new PolicyHandler(resolver, bookingRepository, Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));
    }

    @Test
    void studentWithinRules_passesAndSetsPolicy() {
        when(bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(eq(1L), any(), any())).thenReturn(1L);
        BookingValidationContext context = context(Role.STUDENT, NOW.plusDays(1), 2, null);

        handler.check(context);

        assertThat(context.getPolicy()).isInstanceOf(StudentBookingPolicy.class);
    }

    @Test
    void studentLongerThanTwoHours_throws() {
        assertThatThrownBy(() -> handler.check(context(Role.STUDENT, NOW.plusDays(1), 3, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 ชั่วโมง");
    }

    @Test
    void studentMoreThanSevenDaysAhead_throws() {
        assertThatThrownBy(() -> handler.check(context(Role.STUDENT, NOW.plusDays(8), 1, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("7 วัน");
    }

    @Test
    void studentWithTwoActiveBookings_throws() {
        when(bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(eq(1L), any(), any())).thenReturn(2L);

        assertThatThrownBy(() -> handler.check(context(Role.STUDENT, NOW.plusDays(1), 1, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 รายการ");
    }

    @Test
    void lecturerFourHoursTwentyDaysAhead_passes() {
        when(bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(anyLong(), any(), any())).thenReturn(0L);

        assertThatCode(() -> handler.check(context(Role.LECTURER, NOW.plusDays(20), 4, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void update_doesNotCountActiveBookings() {
        handler.check(context(Role.STUDENT, NOW.plusDays(1), 1, 99L));

        verifyNoInteractions(bookingRepository);
    }

    private BookingValidationContext context(Role role, LocalDateTime start, int hours, Long excludeId) {
        return BookingValidationContext.builder()
                .userId(1L)
                .role(role)
                .roomId(1L)
                .startTime(start)
                .endTime(start.plusHours(hours))
                .attendees(5)
                .excludeBookingId(excludeId)
                .build();
    }
}
