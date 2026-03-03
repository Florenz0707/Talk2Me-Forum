package com.example.springboot_backend.talk2me.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot_backend.talk2me.model.domain.LikeDO;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.repository.LikeMapper;
import com.example.springboot_backend.talk2me.repository.PostMapper;
import com.example.springboot_backend.talk2me.repository.ReplyMapper;
import com.example.springboot_backend.talk2me.service.ILikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService implements ILikeService {
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final ReplyMapper replyMapper;

    public LikeService(LikeMapper likeMapper, PostMapper postMapper, ReplyMapper replyMapper) {
        this.likeMapper = likeMapper;
        this.postMapper = postMapper;
        this.replyMapper = replyMapper;
    }

    @Override
    @Transactional
    public void like(String targetType, Long targetId, Long userId) {
        LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeDO::getUserId, userId)
               .eq(LikeDO::getTargetType, targetType)
               .eq(LikeDO::getTargetId, targetId);
        if (likeMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Already liked");
        }

        LikeDO like = new LikeDO();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeMapper.insert(like);

        updateLikeCount(targetType, targetId, 1);
    }

    @Override
    @Transactional
    public void unlike(String targetType, Long targetId, Long userId) {
        LambdaQueryWrapper<LikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeDO::getUserId, userId)
               .eq(LikeDO::getTargetType, targetType)
               .eq(LikeDO::getTargetId, targetId);
        likeMapper.delete(wrapper);

        updateLikeCount(targetType, targetId, -1);
    }

    private void updateLikeCount(String targetType, Long targetId, int delta) {
        if ("POST".equals(targetType)) {
            PostDO post = postMapper.selectById(targetId);
            if (post != null) {
                post.setLikeCount(post.getLikeCount() + delta);
                postMapper.updateById(post);
            }
        } else if ("REPLY".equals(targetType)) {
            ReplyDO reply = replyMapper.selectById(targetId);
            if (reply != null) {
                reply.setLikeCount(reply.getLikeCount() + delta);
                replyMapper.updateById(reply);
            }
        }
    }
}
