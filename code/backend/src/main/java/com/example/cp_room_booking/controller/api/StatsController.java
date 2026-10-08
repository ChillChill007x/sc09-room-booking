package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.response.RoomUsageResponse;
import com.example.cp_room_booking.dto.response.StatsSummaryResponse;
import com.example.cp_room_booking.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Stats", description = "สถิติการใช้ห้อง (เจ้าหน้าที่)")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "สรุปจำนวนการจองแยกตามสถานะ")
    @GetMapping("/summary")
    public StatsSummaryResponse summary() {
        return statsService.summary();
    }

    @Operation(summary = "ชั่วโมงใช้งานต่อห้อง", description = "from และ to เป็นวันที่ เช่น 2026-10-01")
    @GetMapping("/room-usage")
    public List<RoomUsageResponse> roomUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statsService.roomUsage(from, to);
    }
}
