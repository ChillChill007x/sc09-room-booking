package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ขั้นที่ 1: เวลาเริ่มก่อนเวลาจบ ไม่จองย้อนหลัง อยู่ในวันเดียวกันและอยู่ในเวลาเปิดอาคาร
 */
@Component
public class TimeRangeHandler extends BookingValidationHandler {

    private final Clock clock;
    private final LocalTime buildingOpen;
    private final LocalTime buildingClose;

    public TimeRangeHandler(Clock clock,
                            @Value("${app.booking.building-open}") LocalTime buildingOpen,
                            @Value("${app.booking.building-close}") LocalTime buildingClose) {
        this.clock = clock;
        this.buildingOpen = buildingOpen;
        this.buildingClose = buildingClose;
    }

    @Override
    protected void check(BookingValidationContext context) {
        LocalDateTime start = context.getStartTime();
        LocalDateTime end = context.getEndTime();
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("เวลาเริ่มต้องมาก่อนเวลาสิ้นสุด");
        }
        if (start.isBefore(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("ไม่สามารถจองเวลาที่ผ่านมาแล้ว");
        }
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessRuleException("การจองต้องเริ่มและจบในวันเดียวกัน");
        }
        if (start.toLocalTime().isBefore(buildingOpen) || end.toLocalTime().isAfter(buildingClose)) {
            throw new BusinessRuleException("จองได้เฉพาะเวลาเปิดอาคาร " + buildingOpen + " - " + buildingClose);
        }
    }
}
