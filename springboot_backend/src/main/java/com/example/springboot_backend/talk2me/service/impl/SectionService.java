package com.example.springboot_backend.talk2me.service.impl;

import com.example.springboot_backend.talk2me.model.domain.SectionDO;
import com.example.springboot_backend.talk2me.repository.SectionMapper;
import com.example.springboot_backend.talk2me.service.ISectionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService implements ISectionService {
    private final SectionMapper sectionMapper;

    public SectionService(SectionMapper sectionMapper) {
        this.sectionMapper = sectionMapper;
    }

    @Override
    public List<SectionDO> listSections() {
        return sectionMapper.selectList(null);
    }

    @Override
    public SectionDO getSection(Long id) {
        return sectionMapper.selectById(id);
    }
}
