package com.example.springboot_backend.talk2me.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springboot_backend.talk2me.model.vo.ReplyDetailResponse;
import com.example.springboot_backend.talk2me.service.IReplyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReplyControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private IReplyService replyService;

  @Test
  void getReplyDetail_PublicAccessPassesNullUserId() throws Exception {
    ReplyDetailResponse response = new ReplyDetailResponse();
    response.setId(1L);
    response.setUserId(2L);
    response.setUsername("reply-user");

    when(replyService.getReplyDetail(1L, null)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/replies/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.user_id").value(2L))
        .andExpect(jsonPath("$.data.username").value("reply-user"));

    verify(replyService).getReplyDetail(1L, null);
  }

  @Test
  @WithMockUser(username = "9")
  void getReplyDetail_WithAuthenticationPassesUserId() throws Exception {
    ReplyDetailResponse response = new ReplyDetailResponse();
    response.setId(1L);
    response.setIsLiked(true);

    when(replyService.getReplyDetail(1L, 9L)).thenReturn(response);

    mockMvc.perform(get("/api/v1/replies/1")).andExpect(status().isOk());

    verify(replyService).getReplyDetail(1L, 9L);
  }
}
