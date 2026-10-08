package com.example.cp_room_booking.service.scheduler;

import com.example.cp_room_booking.service.BookingLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ทำงานทุก 1 นาที (ค่า app.scheduler.fixed-delay-ms) ปิดการจองที่เลยเวลาโดยอัตโนมัติ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingLifecycleService lifecycleService;

    @Scheduled(fixedDelayString = "${app.scheduler.fixed-delay-ms}", initialDelay = 30_000)
    public void closeOverdueBookings() {
        int noShows = lifecycleService.markNoShows();
        int completed = lifecycleService.completeFinished();
        if (noShows > 0 || completed > 0) {
            log.info("Scheduler: NO_SHOW {} รายการ, COMPLETED {} รายการ", noShows, completed);
        }
    }
}
