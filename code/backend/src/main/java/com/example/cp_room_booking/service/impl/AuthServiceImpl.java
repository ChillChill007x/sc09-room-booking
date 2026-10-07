package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.dto.request.LoginRequest;
import com.example.cp_room_booking.dto.request.RegisterRequest;
import com.example.cp_room_booking.dto.response.AuthResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.mapper.UserMapper;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.JwtService;
import com.example.cp_room_booking.security.UserPrincipal;
import com.example.cp_room_booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Set<Role> SELF_REGISTER_ROLES = Set.of(Role.STUDENT, Role.LECTURER);
    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("อีเมล " + email + " ถูกใช้สมัครแล้ว");
        }
        Role role = request.role() == null ? Role.STUDENT : request.role();
        if (!SELF_REGISTER_ROLES.contains(role)) {
            throw new BusinessRuleException("สมัครเองได้เฉพาะนักศึกษาและอาจารย์");
        }
        User saved = userRepository.save(userMapper.toNewUser(request, role, passwordEncoder.encode(request.password())));
        return toAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("อีเมลหรือรหัสผ่านไม่ถูกต้อง"));
        if (!user.isActive()) {
            throw new DisabledException("บัญชีนี้ถูกปิดใช้งาน");
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(UserPrincipal.from(user)))
                .tokenType(TOKEN_TYPE)
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toResponse(user))
                .build();
    }
}
