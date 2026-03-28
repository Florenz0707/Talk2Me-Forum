package com.example.springboot_backend.talk2me.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot_backend.talk2me.model.domain.PostViewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostViewMapper extends BaseMapper<PostViewDO> {}
