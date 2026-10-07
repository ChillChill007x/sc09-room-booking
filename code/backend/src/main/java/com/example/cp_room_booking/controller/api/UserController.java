package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.request.UpdateProfileRequest;
import com.example.cp_room_booking.dto.request.UpdateUserRequest;
import com.example.cp_room_booking.dto.response.UserResponse;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "ข้อมูลผู้ใช้และการจัดการผู้ใช้")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "ดูข้อมูลของตัวเอง")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }

    @Operation(summary = "แก้ profile ของตัวเอง")
    @PutMapping("/me/profile")
    public UserResponse updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMyProfile(principal.getId(), request);
    }

    @Operation(summary = "รายชื่อผู้ใช้ทั้งหมด (เจ้าหน้าที่)", description = "รองรับ page, size, sort และกรอง role")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping
    public PageResponse<UserResponse> findAll(@RequestParam(required = false) Role role,
                                              @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
                                              Pageable pageable) {
        return userService.findAll(role, pageable);
    }

    @Operation(summary = "ดูผู้ใช้ตาม id (เจ้าหน้าที่ หรือเจ้าของ)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "แก้ข้อมูลผู้ใช้ รวม role และสถานะ (เจ้าหน้าที่)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "ปิดใช้งานผู้ใช้ (เปลี่ยนเป็น INACTIVE)")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        userService.deactivate(id);
    }
}
