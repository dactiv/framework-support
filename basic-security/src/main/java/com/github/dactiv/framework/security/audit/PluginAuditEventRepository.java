package com.github.dactiv.framework.security.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 插件审计事件的仓库实现
 *
 * @author maurice.chen
 */
public interface PluginAuditEventRepository extends AuditEventRepository {

    List<String> DEFAULT_IGNORE_PRINCIPALS = List.of("anonymousUser");

    /**
     * 获取分页信息
     *
     * @param pageRequest 分页请求
     * @param principal   当前人
     * @param after       在什么时间之后的
     * @param type        类型
     *
     * @return 分页信息
     */
    Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type);

    /**
     * 通过唯一识别获取数据
     *
     * @param target 唯一识别;
     *
     * @return 审计事件
     */
    AuditEvent get(Object target);

    /**
     * 创建审计事件
     *
     * @param map map 数据源
     *
     * @return 审计事件
     */
    default AuditEvent createAuditEvent(Map<String, Object> map) {
        Instant instant = Instant.ofEpochMilli(Casts.cast(map.get("timestamp"), Long.class));
        String principal = map.get("principal").toString();
        String type = map.get("type").toString();
        //noinspection unchecked
        Map<String, Object> data = Casts.cast(map.get("data"), Map.class);

        return new AuditEvent(instant, principal, type, data);
    }
}
