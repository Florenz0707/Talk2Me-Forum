package com.example.springboot_backend.talk2me.controller;

import com.example.springboot_backend.core.model.Result;
import com.example.springboot_backend.talk2me.model.domain.SectionDO;
import com.example.springboot_backend.talk2me.service.ISectionService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {
  private final ISectionService sectionService;

  public SectionController(ISectionService sectionService) {
    this.sectionService = sectionService;
  }

  @GetMapping
  public Result<List<SectionDO>> listSections() {
    return Result.success(sectionService.listSections());
  }

  @GetMapping("/{id}")
  public Result<SectionDO> getSection(@PathVariable Long id) {
    return Result.success(sectionService.getSection(id));
  }
}
