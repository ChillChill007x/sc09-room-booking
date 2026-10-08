package com.example.cp_room_booking.service.state;

import com.example.cp_room_booking.domain.enums.BookingAction;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ตรวจครบทุกคู่ สถานะ 7 x action 6 = 42 กรณี
 */
class BookingStateTransitionTest {

    /**
     * ตารางที่ถูกต้องตามที่ออกแบบไว้ คู่ที่ไม่อยู่ในตารางต้องทำไม่ได้
     */
    private static final Map<BookingStatus, Map<BookingAction, BookingStatus>> EXPECTED = new EnumMap<>(Map.of(
            BookingStatus.PENDING, Map.of(
                    BookingAction.APPROVE, BookingStatus.APPROVED,
                    BookingAction.REJECT, BookingStatus.REJECTED,
                    BookingAction.CANCEL, BookingStatus.CANCELLED),
            BookingStatus.APPROVED, Map.of(
                    BookingAction.CHECK_IN, BookingStatus.CHECKED_IN,
                    BookingAction.CANCEL, BookingStatus.CANCELLED,
                    BookingAction.MARK_NO_SHOW, BookingStatus.NO_SHOW),
            BookingStatus.CHECKED_IN, Map.of(
                    BookingAction.COMPLETE, BookingStatus.COMPLETED)));

    private static final BookingStateFactory FACTORY = new BookingStateFactory(List.of(
            new PendingState(), new ApprovedState(), new CheckedInState(), new RejectedState(),
            new CancelledState(), new CompletedState(), new NoShowState()));

    static Stream<Arguments> allStatusActionPairs() {
        return Arrays.stream(BookingStatus.values())
                .flatMap(status -> Arrays.stream(BookingAction.values())
                        .map(action -> Arguments.of(status, action,
                                EXPECTED.getOrDefault(status, Map.of()).get(action))));
    }

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @MethodSource("allStatusActionPairs")
    void transition(BookingStatus status, BookingAction action, BookingStatus expectedNext) {
        BookingState state = FACTORY.of(status);

        assertThat(state.status()).isEqualTo(status);
        assertThat(state.canHandle(action)).isEqualTo(expectedNext != null);
        assertThat(state.next(action).orElse(null)).isEqualTo(expectedNext);
    }
}
