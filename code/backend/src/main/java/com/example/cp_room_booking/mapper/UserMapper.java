package com.example.cp_room_booking.mapper;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.entity.UserProfile;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.request.RegisterRequest;
import com.example.cp_room_booking.dto.request.UpdateProfileRequest;
import com.example.cp_room_booking.dto.request.UpdateUserRequest;
import com.example.cp_room_booking.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        UserProfile profile = user.getProfile();
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(profile != null ? profile.getFullName() : null)
                .studentCode(profile != null ? profile.getStudentCode() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .department(profile != null ? profile.getDepartment() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toNewUser(RegisterRequest request, Role role, String passwordHash) {
        User user = User.builder()
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordHash)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        user.attachProfile(UserProfile.builder()
                .fullName(request.fullName().trim())
                .studentCode(blankToNull(request.studentCode()))
                .phone(blankToNull(request.phone()))
                .department(blankToNull(request.department()))
                .build());
        return user;
    }

    public void updateProfile(UserProfile profile, UpdateProfileRequest request) {
        profile.setFullName(request.fullName().trim());
        profile.setStudentCode(blankToNull(request.studentCode()));
        profile.setPhone(blankToNull(request.phone()));
        profile.setDepartment(blankToNull(request.department()));
    }

    public void updateUser(User user, UpdateUserRequest request) {
        user.setRole(request.role());
        user.setStatus(request.status());
        updateProfile(user.getProfile(), new UpdateProfileRequest(
                request.fullName(), request.studentCode(), request.phone(), request.department()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
