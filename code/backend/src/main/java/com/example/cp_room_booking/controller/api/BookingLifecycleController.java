package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.request.StatusChangeRequest;
import com.example.cp_room_booking.dto.response.AllowedActionsResponse;
import com.example.cp_room_booking.dto.response.BookingHistoryResponse;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Booking Lifecycle", description = "อนุมัติ ปฏิเสธ ยกเลิก check-in และประวัติสถานะ")
@RestController
@RequestMapping("/api/v1/bookings/{id}")
@RequiredArgsConstructor
public class BookingLifecycleController {

    private final BookingLifecycleService lifecycleService;

    @Operation(summary = "เปลี่ยนสถานะการจอง",
            description = "action: APPROVE, REJECT (ต้องมี note), CANCEL, CHECK_IN, COMPLETE, MARK_NO_SHOW "
                    + "ไม่มีสิทธิ์ตอบ 403 สถานะปัจจุบันทำไม่ได้ตอบ 409 ผิดกฎเวลาตอบ 400")
    @PatchMapping("/status")
    public BookingResponse changeStatus(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor,
                                        @Valid @RequestBody StatusChangeRequest request) {
        return lifecycleService.changeStatus(id, request.action(), request.note(), actor);
    }

    @Operation(summary = "ประวัติการเปลี่ยนสถานะ (ไทม์ไลน์)")
    @GetMapping("/history")
    public List<BookingHistoryResponse> history(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        return lifecycleService.getHistory(id, actor);
    }

    @Operation(summary = "action ที่ผู้ใช้ปัจจุบันทำได้กับการจองนี้")
    @GetMapping("/allowed-actions")
    public AllowedActionsResponse allowedActions(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserPrincipal actor) {
        return lifecycleService.getAllowedActions(id, actor);
    }
}
