package com.example.cp_room_booking.service.policy;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * เลือก BookingPolicy ตาม role โดยไม่มี if-else
 * Spring ส่ง BookingPolicy ทุกตัวเข้ามาทาง constructor แล้วสร้างเป็น Map ครั้งเดียว
 */
@Component
public class BookingPolicyResolver {

    private final Map<Role, BookingPolicy> policies;

    public BookingPolicyResolver(List<BookingPolicy> policyList) {
        this.policies = policyList.stream()
                .flatMap(policy -> policy.supportedRoles().stream().map(role -> Map.entry(role, policy)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public BookingPolicy resolve(Role role) {
        return Optional.ofNullable(policies.get(role))
                .orElseThrow(() -> new BusinessRuleException("ไม่มีกฎการจองสำหรับบทบาท " + role));
    }
}
