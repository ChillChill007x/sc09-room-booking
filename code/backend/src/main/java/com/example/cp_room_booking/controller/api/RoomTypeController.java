package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.request.RoomTypeRequest;
import com.example.cp_room_booking.dto.response.RoomTypeResponse;
import com.example.cp_room_booking.service.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Room Types", description = "ประเภทห้อง")
@RestController
@RequestMapping("/api/v1/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @Operation(summary = "รายการประเภทห้อง")
    @GetMapping
    public List<RoomTypeResponse> findAll() {
        return roomTypeService.findAll();
    }

    @Operation(summary = "ดูประเภทห้องตาม id")
    @GetMapping("/{id}")
    public RoomTypeResponse findById(@PathVariable Long id) {
        return roomTypeService.getById(id);
    }

    @Operation(summary = "เพิ่มประเภทห้อง (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomTypeResponse create(@Valid @RequestBody RoomTypeRequest request) {
        return roomTypeService.create(request);
    }

    @Operation(summary = "แก้ไขประเภทห้อง (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}")
    public RoomTypeResponse update(@PathVariable Long id, @Valid @RequestBody RoomTypeRequest request) {
        return roomTypeService.update(id, request);
    }

    @Operation(summary = "ลบประเภทห้อง (เจ้าหน้าที่)", description = "ยังมีห้องใช้ประเภทนี้ตอบ 409")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomTypeService.delete(id);
    }
}
