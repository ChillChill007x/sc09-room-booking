package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.dto.request.RoomClosureRequest;
import com.example.cp_room_booking.dto.response.RoomClosureResponse;
import com.example.cp_room_booking.service.RoomClosureService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Room Closures", description = "ช่วงปิดห้องเพื่อซ่อมบำรุง")
@RestController
@RequestMapping("/api/v1/rooms/{roomId}/closures")
@RequiredArgsConstructor
public class RoomClosureController {

    private final RoomClosureService roomClosureService;

    @Operation(summary = "ช่วงปิดของห้อง")
    @GetMapping
    public List<RoomClosureResponse> findByRoom(@PathVariable Long roomId) {
        return roomClosureService.findByRoom(roomId);
    }

    @Operation(summary = "เพิ่มช่วงปิดห้อง (เจ้าหน้าที่)", description = "ทับกับช่วงปิดเดิมตอบ 409")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomClosureResponse create(@PathVariable Long roomId, @Valid @RequestBody RoomClosureRequest request) {
        return roomClosureService.create(roomId, request);
    }

    @Operation(summary = "ลบช่วงปิดห้อง (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @DeleteMapping("/{closureId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long roomId, @PathVariable Long closureId) {
        roomClosureService.delete(roomId, closureId);
    }
}
