package com.example.springboot_backend.talk2me.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot_backend.talk2me.model.domain.UserStatsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStatsDO> {}
