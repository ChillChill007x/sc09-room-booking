package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.request.EquipmentRequest;
import com.example.cp_room_booking.dto.response.EquipmentResponse;
import com.example.cp_room_booking.service.EquipmentService;
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

@Tag(name = "Equipment", description = "อุปกรณ์ในห้อง")
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "รายการอุปกรณ์ทั้งหมด")
    @GetMapping
    public List<EquipmentResponse> findAll() {
        return equipmentService.findAll();
    }

    @Operation(summary = "ดูอุปกรณ์ตาม id")
    @GetMapping("/{id}")
    public EquipmentResponse findById(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    @Operation(summary = "เพิ่มอุปกรณ์ (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.create(request);
    }

    @Operation(summary = "แก้ไขอุปกรณ์ (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @Operation(summary = "ลบอุปกรณ์ (เจ้าหน้าที่)", description = "ยังติดตั้งในห้องอยู่ตอบ 409")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }
}
