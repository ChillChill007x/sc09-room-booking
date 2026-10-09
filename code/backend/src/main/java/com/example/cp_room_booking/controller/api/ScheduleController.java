package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.response.CalendarDayResponse;
import com.example.cp_room_booking.dto.response.ScheduleSlotResponse;
import com.example.cp_room_booking.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Schedule", description = "ปฏิทินและตารางการใช้ห้องของทั้งอาคาร (ผู้ใช้ที่ login)")
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "จำนวนการจองรายวันของเดือน", description = "month เช่น 2026-10 คืนเฉพาะวันที่มีการจอง")
    @GetMapping("/month")
    public List<CalendarDayResponse> month(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return scheduleService.monthSummary(month);
    }

    @Operation(summary = "การจองทุกห้องของวันที่เลือก", description = "date เช่น 2026-10-12 ไม่เปิดเผยข้อมูลผู้จอง")
    @GetMapping("/day")
    public List<ScheduleSlotResponse> day(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.daySchedule(date);
    }
}
