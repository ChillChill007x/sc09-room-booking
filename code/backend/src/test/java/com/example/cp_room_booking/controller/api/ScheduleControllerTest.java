package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.response.CalendarDayResponse;
import com.example.cp_room_booking.dto.response.ScheduleSlotResponse;
import com.example.cp_room_booking.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    void month_parsesYearMonthAndReturnsDayCounts() throws Exception {
        when(scheduleService.monthSummary(YearMonth.of(2026, 10))).thenReturn(List.of(
                CalendarDayResponse.builder().date(LocalDate.of(2026, 10, 12)).bookings(3).build()));

        mockMvc.perform(get("/api/v1/schedule/month").param("month", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-10-12"))
                .andExpect(jsonPath("$[0].bookings").value(3));
    }

    @Test
    void month_invalidFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/schedule/month").param("month", "10-2026"))
                .andExpect(status().isBadRequest());
        verify(scheduleService, never()).monthSummary(any());
    }

    @Test
    void day_returnsSlotsWithRoomCode() throws Exception {
        LocalDate day = LocalDate.of(2026, 10, 12);
        when(scheduleService.daySchedule(day)).thenReturn(List.of(ScheduleSlotResponse.builder()
                .bookingId(9L).roomId(2L).roomCode("SC09-9226")
                .startTime(day.atTime(9, 0)).endTime(day.atTime(11, 0)).status(BookingStatus.APPROVED).build()));

        mockMvc.perform(get("/api/v1/schedule/day").param("date", "2026-10-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("SC09-9226"))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void day_missingDate_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/schedule/day")).andExpect(status().isBadRequest());
    }
}
