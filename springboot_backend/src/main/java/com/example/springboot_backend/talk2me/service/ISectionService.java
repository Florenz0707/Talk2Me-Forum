package com.example.springboot_backend.talk2me.service;

import com.example.springboot_backend.talk2me.model.domain.SectionDO;
import java.util.List;

public interface ISectionService {
  List<SectionDO> listSections();

  SectionDO getSection(Long id);
}
