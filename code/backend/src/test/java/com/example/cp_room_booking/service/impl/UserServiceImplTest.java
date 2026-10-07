package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.entity.UserProfile;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.request.UpdateProfileRequest;
import com.example.cp_room_booking.dto.request.UpdateUserRequest;
import com.example.cp_room_booking.dto.response.UserResponse;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.UserMapper;
import com.example.cp_room_booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, new UserMapper());
    }

    @Test
    void getById_missingUser_throwsNotFound() {
        when(userRepository.findWithProfileById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMyProfile_changesProfileFields() {
        User user = user();
        when(userRepository.findWithProfileById(4L)).thenReturn(Optional.of(user));

        UserResponse response = userService.updateMyProfile(4L,
                new UpdateProfileRequest("ชื่อใหม่", "6733800001", "0811111111", "IT"));

        assertThat(response.fullName()).isEqualTo("ชื่อใหม่");
        assertThat(user.getProfile().getPhone()).isEqualTo("0811111111");
        assertThat(user.getProfile().getDepartment()).isEqualTo("IT");
    }

    @Test
    void updateUser_changesRoleAndStatus() {
        User user = user();
        when(userRepository.findWithProfileById(4L)).thenReturn(Optional.of(user));

        userService.updateUser(4L, new UpdateUserRequest(Role.LECTURER, UserStatus.ACTIVE, "อาจารย์ใหม่",
                null, "", null));

        assertThat(user.getRole()).isEqualTo(Role.LECTURER);
        assertThat(user.getProfile().getFullName()).isEqualTo("อาจารย์ใหม่");
        assertThat(user.getProfile().getPhone()).isNull();
    }

    @Test
    void deactivate_setsStatusInactive() {
        User user = user();
        when(userRepository.findWithProfileById(4L)).thenReturn(Optional.of(user));

        userService.deactivate(4L);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    private User user() {
        User user = User.builder()
                .id(4L)
                .email("student@kkumail.com")
                .passwordHash("hash")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
        user.attachProfile(UserProfile.builder().fullName("ชื่อเดิม").build());
        return user;
    }
}
