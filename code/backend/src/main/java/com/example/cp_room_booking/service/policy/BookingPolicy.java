package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;

import java.util.Set;

/**
 * Strategy: กฎการจองของแต่ละ role
 * เพิ่ม role ใหม่ทำได้ด้วยการเพิ่ม class ใหม่ ไม่ต้องแก้โค้ดเดิม (Open/Closed)
 */
public interface BookingPolicy {

    Set<Role> supportedRoles();

    int maxDurationHours();

    int maxAdvanceDays();

    int maxActiveBookings();

    boolean requiresApproval();
}
