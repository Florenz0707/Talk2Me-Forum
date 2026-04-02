package com.example.springboot_backend.talk2me.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springboot_backend.core.security.UserDetailsServiceImpl;
import com.example.springboot_backend.talk2me.service.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private INotificationService notificationService;

  @Test
  void markAllRead_UsesAllAsDefaultType() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/notifications/read-all").with(authentication(buildAuthentication(1L))))
        .andExpect(status().isOk());

    verify(notificationService).markAllRead(1L, "all");
  }

  @Test
  void markAllRead_PassesExplicitType() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/notifications/read-all")
                .param("type", "LIKE_REPLY")
                .with(authentication(buildAuthentication(1L))))
        .andExpect(status().isOk());

    verify(notificationService).markAllRead(1L, "LIKE_REPLY");
  }

  private UsernamePasswordAuthenticationToken buildAuthentication(Long userId) {
    UserDetailsServiceImpl.UserPrincipal principal =
        new UserDetailsServiceImpl.UserPrincipal(
            userId, "user-" + userId, "password", java.util.List.of(), true);
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }
}
