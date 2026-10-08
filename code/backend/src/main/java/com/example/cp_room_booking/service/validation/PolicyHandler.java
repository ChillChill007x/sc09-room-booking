package com.example.cp_room_booking.service.validation;

import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.repository.BookingRepository;
import com.example.cp_room_booking.service.policy.BookingPolicy;
import com.example.cp_room_booking.service.policy.BookingPolicyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * ขั้นที่ 2: ตรวจตามกฎของ role ผ่าน BookingPolicy (Strategy ของคนที่ 1)
 * ระยะเวลาจอง จองล่วงหน้าได้ไกลแค่ไหน และจำนวนการจองที่ค้างอยู่
 */
@Component
@RequiredArgsConstructor
public class PolicyHandler extends BookingValidationHandler {

    private final BookingPolicyResolver policyResolver;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    @Override
    protected void check(BookingValidationContext context) {
        BookingPolicy policy = policyResolver.resolve(context.getRole());
        context.setPolicy(policy);

        if (context.duration().toMinutes() > policy.maxDurationHours() * 60L) {
            throw new BusinessRuleException("จองได้ครั้งละไม่เกิน " + policy.maxDurationHours() + " ชั่วโมง");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (context.getStartTime().isAfter(now.plusDays(policy.maxAdvanceDays()))) {
            throw new BusinessRuleException("จองล่วงหน้าได้ไม่เกิน " + policy.maxAdvanceDays() + " วัน");
        }
        if (!context.isUpdate()) {
            long active = bookingRepository.countByUserIdAndStatusInAndEndTimeAfter(
                    context.getUserId(), BookingStatus.ACTIVE, now);
            if (active >= policy.maxActiveBookings()) {
                throw new BusinessRuleException("มีการจองที่ยังไม่สิ้นสุดครบ " + policy.maxActiveBookings()
                        + " รายการแล้ว");
            }
        }
    }
}
