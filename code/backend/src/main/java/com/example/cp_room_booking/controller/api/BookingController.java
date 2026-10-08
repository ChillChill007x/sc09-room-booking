package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.BookingStatus;
import com.example.cp_room_booking.dto.request.BookingRequest;
import com.example.cp_room_booking.dto.response.BookingResponse;
import com.example.cp_room_booking.dto.response.BookingSlotResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Bookings", description = "สร้าง ดู แก้ไข และลบการจอง")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "สร้างการจอง",
            description = "ตรวจกฎผ่าน Chain of Responsibility เวลาชนหรือห้องปิดตอบ 409 ผิดกฎตอบ 400")
    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal UserPrincipal actor,
                                  @Valid @RequestBody BookingRequest request) {
        return bookingService.create(actor, request);
    }

    @Operation(summary = "การจองทั้งหมด (เจ้าหน้าที่)", description = "กรองตาม status, roomId และช่วงวันที่ from-to")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/bookings")
    public PageResponse<BookingResponse> findAll(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return bookingService.findAll(status, roomId, from, to, pageable);
    }

    @Operation(summary = "รายละเอียดการจอง (เจ้าของหรือเจ้าหน้าที่)")
    @GetMapping("/bookings/{id}")
    public BookingResponse findById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        return bookingService.getById(id, actor);
    }

    @Operation(summary = "แก้ไขการจอง", description = "เฉพาะเจ้าของ และเฉพาะสถานะ PENDING ไม่เช่นนั้นตอบ 409")
    @PutMapping("/bookings/{id}")
    public BookingResponse update(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor,
                                  @Valid @RequestBody BookingRequest request) {
        return bookingService.update(id, actor, request);
    }

    @Operation(summary = "ลบการจอง", description = "เฉพาะเจ้าของ และเฉพาะสถานะ PENDING ไม่เช่นนั้นตอบ 409")
    @DeleteMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal actor) {
        bookingService.delete(id, actor);
    }

    @Operation(summary = "การจองของผู้ใช้ (ตัวเองหรือเจ้าหน้าที่)")
    @GetMapping("/users/{userId}/bookings")
    public PageResponse<BookingResponse> findByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal actor,
            @ParameterObject @PageableDefault(size = 10, sort = "startTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return bookingService.findByUser(userId, actor, pageable);
    }

    @Operation(summary = "ตารางการจองของห้องในวันที่เลือก")
    @GetMapping("/rooms/{roomId}/bookings")
    public List<BookingSlotResponse> findRoomSchedule(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookingService.findRoomSchedule(roomId, date);
    }
}
