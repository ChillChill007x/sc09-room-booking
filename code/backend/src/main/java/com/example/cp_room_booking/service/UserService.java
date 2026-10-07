package com.example.cp_room_booking.service;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.request.UpdateProfileRequest;
import com.example.cp_room_booking.dto.request.UpdateUserRequest;
import com.example.cp_room_booking.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getById(Long id);

    PageResponse<UserResponse> findAll(Role role, Pageable pageable);

    UserResponse updateMyProfile(Long userId, UpdateProfileRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deactivate(Long id);
}
