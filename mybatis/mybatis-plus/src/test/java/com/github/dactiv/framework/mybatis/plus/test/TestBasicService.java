package com.github.dactiv.framework.mybatis.plus.test;

import com.github.dactiv.framework.mybatis.plus.test.entity.AllTypeEntity;
import com.github.dactiv.framework.mybatis.plus.test.service.AllTypeEntityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestBasicService {

    @Autowired
    private AllTypeEntityService allTypeEntityService;

    @Test
    public void testAll() {
        long before = allTypeEntityService.count();

        AllTypeEntity entity = allTypeEntityService.get(1);

        long after = allTypeEntityService.count();
    }
}
