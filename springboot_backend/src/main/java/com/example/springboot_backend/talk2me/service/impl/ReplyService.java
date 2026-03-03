package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.vo.CreateReplyRequest;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.service.IReplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplyService implements IReplyService {
    private final ReplyMapper replyMapper;
    private final PostMapper postMapper;

    public ReplyService(ReplyMapper replyMapper, PostMapper postMapper) {
        this.replyMapper = replyMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional
    public ReplyDO createReply(Long postId, CreateReplyRequest request, Long userId) {
        PostDO post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }

        LambdaQueryWrapper<ReplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReplyDO::getPostId, postId)
               .orderByDesc(ReplyDO::getFloorNumber)
               .last("LIMIT 1");
        ReplyDO lastReply = replyMapper.selectOne(wrapper);
        int floorNumber = lastReply == null ? 1 : lastReply.getFloorNumber() + 1;

        ReplyDO reply = new ReplyDO();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(request.getContent());
        reply.setFloorNumber(floorNumber);
        reply.setLikeCount(0);
        reply.setStatus(0);
        replyMapper.insert(reply);

        post.setReplyCount(post.getReplyCount() + 1);
        postMapper.updateById(post);

        return reply;
    }

    @Override
    public Page<ReplyDO> listReplies(Long postId, Integer page, Integer size) {
        Page<ReplyDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ReplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReplyDO::getPostId, postId)
               .eq(ReplyDO::getStatus, 0)
               .orderByAsc(ReplyDO::getFloorNumber);
        return replyMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public void deleteReply(Long id, Long userId) {
        ReplyDO reply = replyMapper.selectById(id);
        if (reply == null || !reply.getUserId().equals(userId)) {
            throw new RuntimeException("Reply not found or not authorized");
        }
        reply.setStatus(1);
        replyMapper.updateById(reply);
    }
}
