package com.github.dactiv.framework.mybatis.plus.test.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.mybatis.plus.test.entity.AllTypeEntity;
import com.github.dactiv.framework.mybatis.plus.test.mapper.AllTypeEntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class AllTypeEntityService extends BasicService<AllTypeEntityMapper, AllTypeEntity> {
}
