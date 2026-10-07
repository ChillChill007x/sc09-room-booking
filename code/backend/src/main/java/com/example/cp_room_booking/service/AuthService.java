package com.example.cp_room_booking.service;

import com.example.cp_room_booking.dto.request.LoginRequest;
import com.example.cp_room_booking.dto.request.RegisterRequest;
import com.example.cp_room_booking.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
