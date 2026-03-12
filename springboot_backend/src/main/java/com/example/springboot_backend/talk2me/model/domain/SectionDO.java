package com.example.springboot_backend.talk2me.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.springboot_backend.core.model.BaseEntity;

@TableName("sections")
public class SectionDO extends BaseEntity {
  private String name;
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
