package com.example.cp_room_booking.service.impl;

import com.example.cp_room_booking.domain.entity.User;
import com.example.cp_room_booking.domain.entity.UserProfile;
import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.request.LoginRequest;
import com.example.cp_room_booking.dto.request.RegisterRequest;
import com.example.cp_room_booking.dto.response.AuthResponse;
import com.example.cp_room_booking.exception.BusinessRuleException;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.mapper.UserMapper;
import com.example.cp_room_booking.repository.UserRepository;
import com.example.cp_room_booking.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, new UserMapper());
    }

    @Test
    void register_newEmail_savesStudentWithHashedPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("New@KKUmail.com", "Password123!", "ผู้ใช้ใหม่",
                "6733800099", null, null, null);
        when(userRepository.existsByEmail("new@kkumail.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@kkumail.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.getProfile().getUser()).isSameAs(saved);
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().fullName()).isEqualTo("ผู้ใช้ใหม่");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest("student@kkumail.com", "Password123!", "ซ้ำ",
                null, null, null, null);
        when(userRepository.existsByEmail("student@kkumail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_staffRole_throwsBusinessRule() {
        RegisterRequest request = new RegisterRequest("x@kkumail.com", "Password123!", "x",
                null, null, null, Role.STAFF);
        when(userRepository.existsByEmail("x@kkumail.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void login_correctPassword_returnsToken() {
        User user = user(UserStatus.ACTIVE);
        when(userRepository.findByEmail("student@kkumail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("student@kkumail.com", "Password123!"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userRepository.findByEmail("student@kkumail.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("student@kkumail.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_inactiveUser_throwsDisabled() {
        when(userRepository.findByEmail("student@kkumail.com")).thenReturn(Optional.of(user(UserStatus.INACTIVE)));
        when(passwordEncoder.matches("Password123!", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("student@kkumail.com", "Password123!")))
                .isInstanceOf(DisabledException.class);
    }

    private User user(UserStatus status) {
        User user = User.builder()
                .id(4L)
                .email("student@kkumail.com")
                .passwordHash("hashed")
                .role(Role.STUDENT)
                .status(status)
                .build();
        user.attachProfile(UserProfile.builder().fullName("นักศึกษา ทดสอบ").build());
        return user;
    }
}
