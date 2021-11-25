package com.github.dactiv.framework.mybatis.plus.test;

import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.mybatis.plus.test.entity.AllTypeEntity;
import com.github.dactiv.framework.mybatis.plus.test.service.AllTypeEntityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestBasicService {

    @Autowired
    private AllTypeEntityService allTypeEntityService;

    @Test
    public void testAllType() {

        AllTypeEntity entity = allTypeEntityService.get(1);

        Assertions.assertEquals(entity.getStatus(), DisabledOrEnabled.Enabled);
        Assertions.assertEquals(entity.getDevice().size(), 2);

        Assertions.assertTrue(
                entity
                        .getEntities()
                        .stream()
                        .allMatch(c -> StringIdEntity.class.isAssignableFrom(c.getClass()))
        );

        Assertions.assertTrue(
                entity
                        .getExecutes()
                        .containsAll(List.of(ExecuteStatus.Processing, ExecuteStatus.Success, ExecuteStatus.Retrying))
        );

        Assertions.assertEquals(entity.getStatus(), DisabledOrEnabled.Enabled);
    }

}
