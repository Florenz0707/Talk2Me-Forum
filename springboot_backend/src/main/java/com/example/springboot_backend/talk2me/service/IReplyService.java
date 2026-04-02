package com.example.springboot_backend.talk2me.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.model.vo.ReplyDetailResponse;

public interface IReplyService {
  ReplyDO createReply(Long postId, CreateReplyRequest request, Long userId);

  Page<ReplyDO> listReplies(Long postId, Integer page, Integer size, Long currentUserId);

  ReplyDetailResponse getReplyDetail(Long replyId, Long currentUserId);

  void deleteReply(Long id, Long userId);
}
