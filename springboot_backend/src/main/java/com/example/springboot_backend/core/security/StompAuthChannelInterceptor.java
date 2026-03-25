package com.example.springboot_backend.core.security;

import java.security.Principal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider tokenProvider;
  private final UserDetailsServiceImpl userDetailsService;

  public StompAuthChannelInterceptor(
      JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
      return message;
    }

    String authHeader = resolveAuthorizationHeader(accessor);
    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Missing Authorization header in STOMP CONNECT");
    }

    String token = authHeader.substring(7);
    if (!tokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("Invalid token in STOMP CONNECT");
    }

    String subject = tokenProvider.getUsernameFromToken(token);
    UserDetailsServiceImpl.UserPrincipal userPrincipal = loadPrincipalBySubject(subject);
    Principal wsPrincipal = () -> String.valueOf(userPrincipal.getId());
    accessor.setUser(wsPrincipal);
    return message;
  }

  private UserDetailsServiceImpl.UserPrincipal loadPrincipalBySubject(String subject) {
    if (!StringUtils.hasText(subject)) {
      throw new IllegalArgumentException("JWT subject is empty");
    }
    if (isNumeric(subject)) {
      return (UserDetailsServiceImpl.UserPrincipal)
          userDetailsService.loadUserById(Long.parseLong(subject));
    }
    return (UserDetailsServiceImpl.UserPrincipal) userDetailsService.loadUserByUsername(subject);
  }

  private String resolveAuthorizationHeader(StompHeaderAccessor accessor) {
    String header = accessor.getFirstNativeHeader("Authorization");
    if (!StringUtils.hasText(header)) {
      header = accessor.getFirstNativeHeader("authorization");
    }
    return header;
  }

  private boolean isNumeric(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
