package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateFactoryTest {

    @Test
    void of_returnsStateMatchingStatus() {
        BookingStateFactory factory = new BookingStateFactory(List.of(
                new PendingState(), new ApprovedState(), new CheckedInState(), new RejectedState(),
                new CancelledState(), new CompletedState(), new NoShowState()));

        for (BookingStatus status : BookingStatus.values()) {
            assertThat(factory.of(status).status()).isEqualTo(status);
        }
        assertThat(factory.of(BookingStatus.PENDING)).isInstanceOf(PendingState.class);
    }

    @Test
    void constructor_missingState_failsFast() {
        assertThatThrownBy(() -> new BookingStateFactory(List.of(new PendingState())))
                .isInstanceOf(IllegalStateException.class);
    }
}
