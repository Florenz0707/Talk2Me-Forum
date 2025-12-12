package com.example.springboot_backend.talk2me.service;

import com.example.springboot_backend.talk2me.model.vo.*;

public interface IAuthService {
    RegisterResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    RefreshResponse refreshToken(String refreshToken);

    VerificationResponse verification();
}
