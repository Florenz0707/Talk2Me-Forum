package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot_backend.core.security.JwtTokenProvider;
import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.model.domain.UserDO;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import com.example.springboot_backend.talk2me.model.vo.*;
import com.example.springboot_backend.talk2me.repository.UserMapper;
import com.example.springboot_backend.talk2me.repository.UserStatsMapper;
import com.example.springboot_backend.talk2me.service.IAuthService;
import java.time.LocalDateTime;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements IAuthService {
  private final UserMapper userMapper;
  private final UserStatsMapper userStatsMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsServiceImpl userDetailsService;

  public AuthService(
      UserMapper userMapper,
      UserStatsMapper userStatsMapper,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider tokenProvider,
      AuthenticationManager authenticationManager,
      UserDetailsServiceImpl userDetailsService) {
    this.userMapper = userMapper;
    this.userStatsMapper = userStatsMapper;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
  }

  @Override
  @Transactional
  public RegisterResponse register(RegisterRequest registerRequest) {
    Long count =
        userMapper.selectCount(
            new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, registerRequest.getUsername()));
    if (count > 0) {
      throw new RuntimeException("Username is already taken!");
    }

    UserDO user = new UserDO();
    user.setUsername(registerRequest.getUsername());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setEnabled(true);

    userMapper.insert(user);

    UserStatsDO stats = new UserStatsDO();
    stats.setUserId(user.getId());
    stats.setLikeCount(0);
    stats.setFollowerCount(0);
    stats.setFollowingCount(0);
    stats.setCreateTime(LocalDateTime.now());
    stats.setUpdateTime(LocalDateTime.now());
    userStatsMapper.insert(stats);

    return new RegisterResponse("User registered successfully");
  }

  @Override
  @Transactional
  public AuthResponse login(LoginRequest loginRequest) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

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

    String username = tokenProvider.getUsernameFromToken(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    String newAccessToken = tokenProvider.generateToken(authentication);
    String newRefreshToken = tokenProvider.generateRefreshToken(username);

    return new RefreshResponse(newAccessToken, newRefreshToken, "Token refreshed successfully");
  }

  @Override
  public VerificationResponse verification() {
    // 验证access_token的正确性
    // 如果请求能到达这里，说明JWT过滤器已经验证了token的有效性
    return new VerificationResponse("Token is valid");
  }
}
