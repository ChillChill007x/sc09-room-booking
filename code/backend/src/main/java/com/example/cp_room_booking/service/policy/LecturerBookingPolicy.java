package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * อาจารย์: ครั้งละไม่เกิน 4 ชั่วโมง ล่วงหน้าไม่เกิน 30 วัน ค้างได้ 5 รายการ อนุมัติอัตโนมัติ
 */
@Component
public class LecturerBookingPolicy implements BookingPolicy {

    @Override
    public Set<Role> supportedRoles() {
        return Set.of(Role.LECTURER);
    }

    @Override
    public int maxDurationHours() {
        return 4;
    }

    @Override
    public int maxAdvanceDays() {
        return 30;
    }

    @Override
    public int maxActiveBookings() {
        return 5;
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }
}
