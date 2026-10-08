package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingPolicyResolverTest {

    private final BookingPolicyResolver resolver = new BookingPolicyResolver(List.of(
            new StudentBookingPolicy(), new LecturerBookingPolicy(), new StaffBookingPolicy()));

    @ParameterizedTest
    @CsvSource({
            "STUDENT, StudentBookingPolicy",
            "LECTURER, LecturerBookingPolicy",
            "STAFF, StaffBookingPolicy",
            "ADMIN, StaffBookingPolicy"
    })
    void resolve_returnsPolicyForRole(Role role, String expectedClass) {
        assertThat(resolver.resolve(role).getClass().getSimpleName()).isEqualTo(expectedClass);
    }

    @Test
    void resolve_roleWithoutPolicy_throwsBusinessRule() {
        BookingPolicyResolver studentOnly = new BookingPolicyResolver(List.of(new StudentBookingPolicy()));

        assertThatThrownBy(() -> studentOnly.resolve(Role.LECTURER)).isInstanceOf(BusinessRuleException.class);
    }
}
