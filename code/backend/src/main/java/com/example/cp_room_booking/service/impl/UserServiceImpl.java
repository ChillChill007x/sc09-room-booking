package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.common.PageResponse;
import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.request.UpdateProfileRequest;
import com.example.cp_room_booking.dto.request.UpdateUserRequest;
import com.example.cp_room_booking.dto.response.UserResponse;
import com.example.cp_room_booking.exception.ResourceNotFoundException;
import com.example.cp_room_booking.mapper.UserMapper;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Role role, Pageable pageable) {
        Page<User> page = role == null
                ? userRepository.findAll(pageable)
                : userRepository.findAllByRole(role, pageable);
        return PageResponse.from(page.map(userMapper::toResponse));
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        userMapper.updateProfile(user.getProfile(), request);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUser(id);
        userMapper.updateUser(user, request);
        return userMapper.toResponse(user);
    }

    /**
     * ไม่ลบจริงเพราะผู้ใช้มีประวัติการจองอ้างอิงอยู่ เปลี่ยนเป็น INACTIVE แทน
     */
    @Override
    @Transactional
    public void deactivate(Long id) {
        findUser(id).setStatus(UserStatus.INACTIVE);
    }

    private User findUser(Long id) {
        return userRepository.findWithProfileById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ผู้ใช้", id));
    }
}
