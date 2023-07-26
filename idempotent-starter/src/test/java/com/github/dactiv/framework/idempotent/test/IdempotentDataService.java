package com.github.dactiv.framework.idempotent.test;

import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class IdempotentDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentDataService.class);

    private final Map<Integer, List<Entity>> data = new LinkedHashMap<>();

    @Idempotent(key = "idempotent:save-entity:[#userId]", value = "[T(java.util.Objects).hash(#entity.name, #entity.type)]")
    public Integer saveEntity(Entity entity, Integer userId) {

        LOGGER.info("保存用户 ID 为 [" + userId + "] 的 entity 数据");
        return save(entity, userId);
    }

    @Idempotent(key = "idempotent:save-entity:[#userId]", value = "[T(java.util.Objects).hash(#entity.name, #entity.type)]", exception = "不要重复提交")
    public Integer saveEntityExceptionMessage(Entity entity, Integer userId) {
        LOGGER.info("保存用户 ID 为 [" + userId + "] 的 entity 数据");
        return save(entity, userId);
    }

    @Idempotent(key = "idempotent:save-entity:[#userId]", value = "[T(java.util.Objects).hash(#entity.name, #entity.type)]", expirationTime = @Time(2000))
    public Integer saveEntityExpirationTime(Entity entity, Integer userId) {
        LOGGER.info("保存用户 ID 为 [" + userId + "] 的 entity 数据");
        return save(entity, userId);
    }

    @Idempotent(key = "idempotent:save-entity:[#userId]", ignore = "userId")
    public Integer saveEntityUnsetValue(String name, int type, Integer userId) {
        LOGGER.info("保存用户 ID 为 [" + userId + "] 的 entity 数据");
        return save(new Entity(name, type, null), userId);
    }

    public void nonIdempotentSaveEntity(Entity entity, Integer userId) {
        LOGGER.info("保存用户 ID 为 [" + userId + "] 的 entity 数据");
        save(entity, userId);
    }

    private Integer save(Entity entity, Integer userId) {
        List<Entity> list = data.computeIfAbsent(userId, k -> new LinkedList<>());
        list.add(entity);

        return list.size();
    }

    public Map<Integer, List<Entity>> getData() {
        return data;
    }
}
