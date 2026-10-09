package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.Booking;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.response.CalendarDayResponse;
import com.example.cp_room_booking.dto.response.ScheduleSlotResponse;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    /**
     * การจองที่ยังใช้งานและที่ใช้ห้องเสร็จแล้ว ไม่รวมรายการที่ถูกปฏิเสธ ยกเลิก หรือไม่มาใช้ห้อง
     */
    static final Set<BookingStatus> SHOWN = EnumSet.of(
            BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.CHECKED_IN, BookingStatus.COMPLETED);

    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarDayResponse> monthSummary(YearMonth month) {
        List<Booking> bookings = bookingRepository.findAllInRange(month.atDay(1).atStartOfDay(),
                month.plusMonths(1).atDay(1).atStartOfDay(), SHOWN);
        // การจองอยู่ในเวลาเปิดอาคารของวันเดียว จึงนับตามวันที่เริ่ม
        Map<LocalDate, Long> perDay = bookings.stream().collect(Collectors.groupingBy(
                booking -> booking.getStartTime().toLocalDate(), TreeMap::new, Collectors.counting()));
        return perDay.entrySet().stream()
                .map(entry -> CalendarDayResponse.builder().date(entry.getKey()).bookings(entry.getValue()).build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleSlotResponse> daySchedule(LocalDate date) {
        return bookingRepository.findAllInRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay(), SHOWN).stream()
                .map(booking -> ScheduleSlotResponse.builder()
                        .bookingId(booking.getId())
                        .roomId(booking.getRoom().getId())
                        .roomCode(booking.getRoom().getCode())
                        .startTime(booking.getStartTime())
                        .endTime(booking.getEndTime())
                        .status(booking.getStatus())
                        .build())
                .toList();
    }
}
