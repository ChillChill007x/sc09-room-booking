package com.example.cp_room_booking.controller.api;

import com.example.cp_room_booking.domain.enums.Role;
import com.example.cp_room_booking.domain.enums.UserStatus;
import com.example.cp_room_booking.dto.response.AuthResponse;
import com.example.cp_room_booking.dto.response.UserResponse;
import com.example.cp_room_booking.exception.ConflictException;
import com.example.cp_room_booking.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String REGISTER_BODY = """
            {"email":"new@kkumail.com","password":"Password123!","fullName":"ผู้ใช้ใหม่"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_validBody_returns201() throws Exception {
        when(authService.register(any())).thenReturn(authResponse());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.user.email").value("new@kkumail.com"));
    }

    @Test
    void register_invalidEmail_returns400WithFieldErrors() throws Exception {
        String body = """
                {"email":"not-an-email","password":"short","fullName":""}
                """;

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any())).thenThrow(new ConflictException("อีเมลถูกใช้แล้ว"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("อีเมลถูกใช้แล้ว"));
    }

    @Test
    void register_thaiPasswordOver72Bytes_returns400InsteadOf500() throws Exception {
        // 25 ตัวอักษรผ่าน @Size แต่เป็น 75 ไบต์ ซึ่ง BCrypt รับไม่ได้
        String body = """
                {"email":"new@kkumail.com","password":"%s","fullName":"ผู้ใช้ใหม่"}
                """.formatted("ก".repeat(25));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
        verify(authService, never()).register(any());
    }

    @Test
    void register_thaiPasswordWithin72Bytes_isAccepted() throws Exception {
        when(authService.register(any())).thenReturn(authResponse());
        String body = """
                {"email":"new@kkumail.com","password":"%s","fullName":"ผู้ใช้ใหม่"}
                """.formatted("ก".repeat(24));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void login_passwordOver72Bytes_returns400InsteadOf500() throws Exception {
        String body = """
                {"email":"student@kkumail.com","password":"%s"}
                """.formatted("ก".repeat(25));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verify(authService, never()).login(any());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("อีเมลหรือรหัสผ่านไม่ถูกต้อง"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@kkumail.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    private AuthResponse authResponse() {
        return AuthResponse.builder()
                .accessToken("jwt")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(UserResponse.builder()
                        .id(10L)
                        .email("new@kkumail.com")
                        .role(Role.STUDENT)
                        .status(UserStatus.ACTIVE)
                        .fullName("ผู้ใช้ใหม่")
                        .build())
                .build();
    }
}
