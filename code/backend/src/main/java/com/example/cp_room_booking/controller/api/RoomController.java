package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.RoomStatus;
import com.example.cp_room_booking.dto.request.RoomEquipmentRequest;
import com.example.cp_room_booking.dto.request.RoomRequest;
import com.example.cp_room_booking.dto.request.RoomSearchRequest;
import com.example.cp_room_booking.dto.response.RoomResponse;
import com.example.cp_room_booking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Rooms", description = "ห้องในอาคาร SC09 ค้นหา กรอง แบ่งหน้า และค้นหาห้องว่าง")
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "รายการห้อง", description = "รองรับ page, size, sort เช่น sort=capacity,desc และตัวกรอง")
    @GetMapping
    public PageResponse<RoomResponse> search(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) @Min(1) Integer minCapacity,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "code", direction = Sort.Direction.ASC)
            Pageable pageable) {
        RoomSearchRequest criteria = new RoomSearchRequest(floor, typeId, minCapacity, equipmentId, keyword, status);
        return roomService.search(criteria, pageable);
    }

    @Operation(summary = "ค้นหาห้องว่าง", description = "เวลาเป็น ISO-8601 เช่น 2026-10-20T09:00:00")
    @GetMapping("/available")
    public List<RoomResponse> findAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) @Min(1) Integer minCapacity,
            @RequestParam(required = false) List<Long> equipmentIds) {
        return roomService.findAvailable(start, end, minCapacity, equipmentIds);
    }

    @Operation(summary = "รายละเอียดห้องพร้อมอุปกรณ์")
    @GetMapping("/{id}")
    public RoomResponse findById(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @Operation(summary = "เพิ่มห้อง (เจ้าหน้าที่)", description = "รหัสห้องซ้ำตอบ 409")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @Operation(summary = "แก้ไขห้อง (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @Operation(summary = "ลบห้อง (เจ้าหน้าที่)", description = "เปลี่ยนเป็น INACTIVE ถ้ามีการจองในอนาคตตอบ 409")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }

    @Operation(summary = "กำหนดอุปกรณ์ในห้อง (เจ้าหน้าที่)", description = "ส่งรายการทั้งหมด รายการที่ไม่ส่งมาจะถูกลบออก")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}/equipment")
    public RoomResponse updateEquipment(@PathVariable Long id,
                                        @RequestBody List<@Valid RoomEquipmentRequest> items) {
        return roomService.updateEquipment(id, items);
    }
}
