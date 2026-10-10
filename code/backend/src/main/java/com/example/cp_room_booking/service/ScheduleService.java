package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.response.CalendarDayResponse;
import com.example.cp_room_booking.dto.response.ScheduleSlotResponse;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * ภาพรวมการจองของทั้งอาคาร: ปฏิทินรายเดือนว่าวันไหนมีการจอง และตารางรวมทุกห้องของวันที่เลือก
 */
public interface ScheduleService {

    /**
     * คืนเฉพาะวันที่มีการจองอย่างน้อย 1 รายการ เรียงตามวันที่
     */
    List<CalendarDayResponse> monthSummary(YearMonth month);

    List<ScheduleSlotResponse> daySchedule(LocalDate date);
}
