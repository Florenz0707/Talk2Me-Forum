package com.example.springboot_backend.talk2me.user.service;

import com.example.springboot_backend.talk2me.user.model.vo.AuthResponse;
import com.example.springboot_backend.talk2me.user.model.vo.LoginRequest;
import com.example.springboot_backend.talk2me.user.model.vo.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(String refreshToken);
}
