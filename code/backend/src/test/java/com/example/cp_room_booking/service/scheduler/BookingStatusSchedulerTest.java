package com.example.cp_room_booking.service.scheduler;

import com.example.cp_room_booking.service.BookingLifecycleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingStatusSchedulerTest {

    @Mock
    private BookingLifecycleService lifecycleService;

    @InjectMocks
    private BookingStatusScheduler scheduler;

    @Test
    void closeOverdueBookings_marksNoShowsThenCompletesFinished() {
        when(lifecycleService.markNoShows()).thenReturn(2);
        when(lifecycleService.completeFinished()).thenReturn(1);

        scheduler.closeOverdueBookings();

        var order = inOrder(lifecycleService);
        order.verify(lifecycleService).markNoShows();
        order.verify(lifecycleService).completeFinished();
    }
}
