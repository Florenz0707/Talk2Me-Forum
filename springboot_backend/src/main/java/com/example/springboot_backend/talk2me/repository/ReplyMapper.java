package com.example.springboot_backend.talk2me.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot_backend.talk2me.model.domain.ReplyDO;
import com.example.springboot_backend.talk2me.model.vo.ReplyDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReplyMapper extends BaseMapper<ReplyDO> {
  @Select(
      """
      SELECT
          r.id,
          r.post_id AS postId,
          r.user_id AS userId,
          u.username AS username,
          r.content AS content,
          r.floor_number AS floorNumber,
          r.like_count AS likeCount,
          p.title AS postTitle,
          p.section_id AS sectionId,
          s.name AS sectionName,
          r.create_time AS createTime,
          r.update_time AS updateTime
      FROM replies r
      INNER JOIN users u ON u.id = r.user_id
      INNER JOIN posts p ON p.id = r.post_id
      INNER JOIN sections s ON s.id = p.section_id
      WHERE r.id = #{replyId}
        AND r.status = 0
        AND p.status = 0
      """)
  ReplyDetailResponse selectReplyDetailById(Long replyId);
}
