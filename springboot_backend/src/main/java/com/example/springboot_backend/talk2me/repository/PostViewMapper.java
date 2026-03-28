package com.example.springboot_backend.talk2me.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot_backend.talk2me.model.domain.PostDO;
import com.example.springboot_backend.talk2me.model.domain.PostViewDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostViewMapper extends BaseMapper<PostViewDO> {

  @Select({
    "<script>",
    "SELECT",
    "  p.id,",
    "  p.section_id,",
    "  p.user_id,",
    "  p.title,",
    "  p.content,",
    "  p.view_count,",
    "  p.like_count,",
    "  p.reply_count,",
    "  p.status,",
    "  p.create_time,",
    "  p.update_time,",
    "  pv.update_time AS lastViewTime",
    "FROM post_views pv",
    "JOIN posts p ON p.id = pv.post_id",
    "WHERE pv.user_id = #{userId}",
    "  AND pv.is_deleted = 0",
    "  AND p.status = 0",
    "ORDER BY pv.update_time",
    "<choose>",
    "  <when test='ascending'>ASC</when>",
    "  <otherwise>DESC</otherwise>",
    "</choose>",
    "</script>"
  })
  Page<PostDO> selectViewedPosts(
      Page<PostDO> page, @Param("userId") Long userId, @Param("ascending") boolean ascending);
}
