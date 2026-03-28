package com.example.springboot_backend.talk2me.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.service.IPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private IPostService postService;

  @Test
  void getPost_PublicAccessPassesNullUserId() throws Exception {
    PostDO response = new PostDO();
    response.setId(1L);

    when(postService.getPost(1L, null)).thenReturn(response);

    mockMvc.perform(get("/api/v1/posts/1")).andExpect(status().isOk());

    verify(postService).getPost(1L, null);
  }

  @Test
  @WithMockUser(username = "1")
  void getPost_WithAuthenticationPassesUserId() throws Exception {
    PostDO response = new PostDO();
    response.setId(1L);

    when(postService.getPost(1L, 1L)).thenReturn(response);

    mockMvc.perform(get("/api/v1/posts/1")).andExpect(status().isOk());

    verify(postService).getPost(1L, 1L);
  }
}
