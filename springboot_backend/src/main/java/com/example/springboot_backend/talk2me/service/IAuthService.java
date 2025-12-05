package com.example.springboot_backend.talk2me.service;

import com.example.springboot_backend.talk2me.model.vo.AuthResponse;
import com.example.springboot_backend.talk2me.model.vo.LoginRequest;
import com.example.springboot_backend.talk2me.model.vo.RefreshResponse;
import com.example.springboot_backend.talk2me.model.vo.RegisterRequest;
import com.example.springboot_backend.talk2me.model.vo.RegisterResponse;
import com.example.springboot_backend.talk2me.model.vo.VerificationResponse;

public interface IAuthService {
    RegisterResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    RefreshResponse refreshToken(String refreshToken);

    VerificationResponse verification();
}
