package com.example.springboot_backend.talk2me.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.springboot_backend.talk2me.model.vo.UpdateProfileRequest;
import com.example.springboot_backend.talk2me.model.vo.UserProfileResponse;
import com.example.springboot_backend.talk2me.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private IUserService userService;

  @Test
  @WithMockUser(username = "1")
  void getCurrentProfile_Success() throws Exception {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(1L);
    response.setUsername("testuser");
    response.setBio("Test bio");

    when(userService.getProfile(1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/users/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("testuser"));
  }

  @Test
  @WithMockUser(username = "1")
  void updateProfile_Success() throws Exception {
    UpdateProfileRequest request = new UpdateProfileRequest();
    request.setBio("Updated bio");
    request.setGender("男");

    UserProfileResponse response = new UserProfileResponse();
    response.setId(1L);
    response.setBio("Updated bio");

    when(userService.updateProfile(eq(1L), any(UpdateProfileRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/api/v1/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.bio").value("Updated bio"));
  }

  @Test
  @WithMockUser
  void getUserProfile_Success() throws Exception {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(2L);
    response.setUsername("otheruser");

    when(userService.getProfile(2L)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/users/2/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("otheruser"));
  }
}
