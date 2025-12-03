package com.example.springboot_backend.talk2me.user.service;

import com.example.springboot_backend.talk2me.user.model.vo.AuthResponse;
import com.example.springboot_backend.talk2me.user.model.vo.LoginRequest;
import com.example.springboot_backend.talk2me.user.model.vo.RefreshResponse;
import com.example.springboot_backend.talk2me.user.model.vo.RegisterRequest;
import com.example.springboot_backend.talk2me.user.model.vo.RegisterResponse;
import com.example.springboot_backend.talk2me.user.model.vo.VerificationResponse;

public interface IAuthService {
    RegisterResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    RefreshResponse refreshToken(String refreshToken);

    VerificationResponse verification();
}
