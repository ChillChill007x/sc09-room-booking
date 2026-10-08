package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ตรวจค่ากฎของ Strategy ทั้ง 3 ตัว
 */
class BookingPolicyTest {

    @Test
    void studentPolicy_twoHoursSevenDaysTwoActive_requiresApproval() {
        BookingPolicy policy = new StudentBookingPolicy();

        assertThat(policy.supportedRoles()).containsExactly(Role.STUDENT);
        assertThat(policy.maxDurationHours()).isEqualTo(2);
        assertThat(policy.maxAdvanceDays()).isEqualTo(7);
        assertThat(policy.maxActiveBookings()).isEqualTo(2);
        assertThat(policy.requiresApproval()).isTrue();
    }

    @Test
    void lecturerPolicy_fourHoursThirtyDaysFiveActive_autoApproved() {
        BookingPolicy policy = new LecturerBookingPolicy();

        assertThat(policy.supportedRoles()).containsExactly(Role.LECTURER);
        assertThat(policy.maxDurationHours()).isEqualTo(4);
        assertThat(policy.maxAdvanceDays()).isEqualTo(30);
        assertThat(policy.maxActiveBookings()).isEqualTo(5);
        assertThat(policy.requiresApproval()).isFalse();
    }

    @Test
    void staffPolicy_coversStaffAndAdmin_unlimitedActive() {
        BookingPolicy policy = new StaffBookingPolicy();

        assertThat(policy.supportedRoles()).containsExactlyInAnyOrder(Role.STAFF, Role.ADMIN);
        assertThat(policy.maxDurationHours()).isEqualTo(8);
        assertThat(policy.maxAdvanceDays()).isEqualTo(90);
        assertThat(policy.maxActiveBookings()).isEqualTo(Integer.MAX_VALUE);
        assertThat(policy.requiresApproval()).isFalse();
    }
}
