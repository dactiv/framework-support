package com.github.dactiv.framework.mybatis.plus.test;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.mybatis.enumerate.OperationDataType;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.plus.audit.EntityIdOperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.framework.mybatis.plus.test.entity.AllTypeEntity;
import com.github.dactiv.framework.mybatis.plus.test.service.AllTypeEntityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestBasicService {

    @Autowired
    private AllTypeEntityService allTypeEntityService;

    @Autowired
    private MybatisPlusOperationDataTraceRepository mybatisPlusOperationDataTraceRepository;

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
                        .containsAll(Arrays.asList(ExecuteStatus.Processing, ExecuteStatus.Success, ExecuteStatus.Retrying))
        );

        Assertions.assertEquals(entity.getStatus(), DisabledOrEnabled.Enabled);
    }

    @Test
    public void testInsertOrUpdate() throws UnknownHostException {
        AllTypeEntity entity = new AllTypeEntity();

        entity.setStatus(DisabledOrEnabled.Disabled);
        entity.setExecutes(Arrays.asList(ExecuteStatus.Failure, ExecuteStatus.Processing));

        allTypeEntityService.save(entity);

        List<OperationDataTraceRecord> data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 1);

        OperationDataTraceRecord record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        EntityIdOperationDataTraceRecord entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.INSERT);

        Map<String, Object> statusValue = Casts.cast(record.getSubmitData().get("status"));
        Assertions.assertEquals(statusValue.get("value"), DisabledOrEnabled.Disabled.getValue());

        allTypeEntityService.lambdaUpdate().set(AllTypeEntity::getStatus, DisabledOrEnabled.Enabled.getValue()).eq(AllTypeEntity::getId, entity.getId()).update();

        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 2);

        record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.UPDATE);

        allTypeEntityService.lambdaUpdate().set(AllTypeEntity::getStatus, DisabledOrEnabled.Disabled.getValue()).eq(AllTypeEntity::getId, entity.getId()).eq(AllTypeEntity::getStatus, DisabledOrEnabled.Disabled.getValue()).update();

        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 2);

        entity.setStatus(DisabledOrEnabled.Disabled);
        allTypeEntityService.updateById(entity);
        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 3);

        record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.UPDATE);

        statusValue = Casts.cast(record.getSubmitData().get("status"));
        Assertions.assertEquals(statusValue.get("value"), DisabledOrEnabled.Disabled.getValue());

        allTypeEntityService.deleteByEntity(entity);
        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 4);

        record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.DELETE);

        entity.setId(null);
        allTypeEntityService.save(entity);

        allTypeEntityService.deleteById(entity.getId());

        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 6);

        record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.DELETE);

        entity.setId(null);
        allTypeEntityService.save(entity);

        allTypeEntityService.lambdaUpdate().eq(AllTypeEntity::getId,entity.getId()).eq(AllTypeEntity::getStatus, DisabledOrEnabled.Disabled.getValue()).remove();
        data = mybatisPlusOperationDataTraceRepository.find("tb_all_type_entity");
        Assertions.assertEquals(data.size(), 8);

        record = data.iterator().next();
        Assertions.assertTrue(EntityIdOperationDataTraceRecord.class.isAssignableFrom(record.getClass()));

        entityRecord = Casts.cast(record);
        Assertions.assertEquals(entityRecord.getEntityId(), entity.getId());

        Assertions.assertEquals(record.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(record.getType(), OperationDataType.DELETE);

    }

}
