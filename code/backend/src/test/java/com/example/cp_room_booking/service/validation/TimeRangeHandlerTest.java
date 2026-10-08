package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRangeHandlerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 14, 8, 0);

    private final TimeRangeHandler handler = new TimeRangeHandler(
            Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE), LocalTime.of(8, 0), LocalTime.of(22, 0));

    @Test
    void validRangeInsideBuildingHours_passes() {
        assertThatCode(() -> handler.check(context("2030-01-15T09:00", "2030-01-15T11:00")))
                .doesNotThrowAnyException();
    }

    @Test
    void endBeforeStart_throws() {
        assertThatThrownBy(() -> handler.check(context("2030-01-15T11:00", "2030-01-15T09:00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("เวลาเริ่มต้องมาก่อน");
    }

    @Test
    void startInThePast_throws() {
        assertThatThrownBy(() -> handler.check(context("2030-01-13T09:00", "2030-01-13T10:00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ผ่านมาแล้ว");
    }

    @Test
    void acrossTwoDays_throws() {
        assertThatThrownBy(() -> handler.check(context("2030-01-15T21:00", "2030-01-16T09:00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("วันเดียวกัน");
    }

    @Test
    void outsideBuildingHours_throws() {
        assertThatThrownBy(() -> handler.check(context("2030-01-15T21:00", "2030-01-15T22:30")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("เวลาเปิดอาคาร");
    }

    static BookingValidationContext context(String start, String end) {
        return BookingValidationContext.builder()
                .userId(1L)
                .role(Role.STUDENT)
                .roomId(1L)
                .startTime(LocalDateTime.parse(start))
                .endTime(LocalDateTime.parse(end))
                .attendees(10)
                .build();
    }
}
