package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * นักศึกษา: ครั้งละไม่เกิน 2 ชั่วโมง ล่วงหน้าไม่เกิน 7 วัน ค้างได้ 2 รายการ ต้องรออนุมัติ
 */
@Component
public class StudentBookingPolicy implements BookingPolicy {

    @Override
    public Set<Role> supportedRoles() {
        return Set.of(Role.STUDENT);
    }

    @Override
    public int maxDurationHours() {
        return 2;
    }

    @Override
    public int maxAdvanceDays() {
        return 7;
    }

    @Override
    public int maxActiveBookings() {
        return 2;
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }
}
