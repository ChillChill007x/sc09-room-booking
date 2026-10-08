package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.response.RoomUsageResponse;
import com.example.cp_room_booking.dto.response.StatsSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {

    StatsSummaryResponse summary();

    List<RoomUsageResponse> roomUsage(LocalDate from, LocalDate to);
}
