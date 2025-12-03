package com.example.springboot_backend.talk2me.user.service;

import com.example.springboot_backend.core.security.JwtTokenProvider;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.user.model.domain.UserDO;
import com.example.springboot_backend.talk2me.user.model.vo.AuthResponse;
import com.example.springboot_backend.talk2me.user.model.vo.LoginRequest;
import com.example.springboot_backend.talk2me.user.model.vo.RefreshResponse;
import com.example.springboot_backend.talk2me.user.model.vo.RegisterRequest;
import com.example.springboot_backend.talk2me.user.model.vo.RegisterResponse;
import com.example.springboot_backend.talk2me.user.model.vo.VerificationResponse;
import com.example.springboot_backend.talk2me.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        UserDO user = new UserDO();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userRepository.save(user);

        return new RegisterResponse("User registered successfully");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);

        UserDetailsServiceImpl.UserPrincipal userPrincipal =
                (UserDetailsServiceImpl.UserPrincipal) authentication.getPrincipal();

        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUsername());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Token is not a refresh token");
        }

        // 返回成功消息
        return new RefreshResponse("Token refreshed successfully");
    }

    @Override
    public VerificationResponse verification() {
        // 验证access_token的正确性
        // 如果请求能到达这里，说明JWT过滤器已经验证了token的有效性
        return new VerificationResponse("Token is valid");
    }
}
