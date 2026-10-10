package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.dto.response.NotificationResponse;
import com.example.cp_room_booking.dto.response.UnreadCountResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notifications", description = "แจ้งเตือนในระบบของผู้ใช้ที่ login อยู่")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "แจ้งเตือนของฉัน (ล่าสุดก่อน)")
    @GetMapping("/users/me/notifications")
    public PageResponse<NotificationResponse> findMine(
            @AuthenticationPrincipal UserPrincipal actor,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return notificationService.findMine(actor.getId(), pageable);
    }

    @Operation(summary = "จำนวนแจ้งเตือนที่ยังไม่อ่าน")
    @GetMapping("/users/me/notifications/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal UserPrincipal actor) {
        return UnreadCountResponse.builder().count(notificationService.countUnread(actor.getId())).build();
    }

    @Operation(summary = "อ่านแจ้งเตือนทั้งหมด")
    @PatchMapping("/users/me/notifications/read-all")
    public UnreadCountResponse markAllRead(@AuthenticationPrincipal UserPrincipal actor) {
        notificationService.markAllRead(actor.getId());
        return UnreadCountResponse.builder().count(0).build();
    }

    @Operation(summary = "อ่านแจ้งเตือน", description = "แจ้งเตือนของคนอื่นตอบ 403")
    @PatchMapping("/notifications/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        return notificationService.markRead(id, actor);
    }

    @Operation(summary = "ลบแจ้งเตือน", description = "แจ้งเตือนของคนอื่นตอบ 403")
    @DeleteMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        notificationService.delete(id, actor);
    }
}
