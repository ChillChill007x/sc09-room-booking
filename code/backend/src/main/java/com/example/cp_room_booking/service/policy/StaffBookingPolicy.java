package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * เจ้าหน้าที่และผู้ดูแลระบบ: ครั้งละไม่เกิน 8 ชั่วโมง ล่วงหน้าไม่เกิน 90 วัน ไม่จำกัดจำนวน อนุมัติอัตโนมัติ
 */
@Component
public class StaffBookingPolicy implements BookingPolicy {

    @Override
    public Set<Role> supportedRoles() {
        return Set.of(Role.STAFF, Role.ADMIN);
    }

    @Override
    public int maxDurationHours() {
        return 8;
    }

    @Override
    public int maxAdvanceDays() {
        return 90;
    }

    @Override
    public int maxActiveBookings() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }
}
