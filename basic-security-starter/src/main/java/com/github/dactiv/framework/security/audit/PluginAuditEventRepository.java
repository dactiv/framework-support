package com.github.dactiv.framework.security.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 插件审计事件的仓库实现
 *
 * @author maurice.chen
 */
public interface PluginAuditEventRepository extends AuditEventRepository {

    List<String> DEFAULT_IGNORE_PRINCIPALS = Collections.singletonList("anonymousUser");

    /**
     * 获取分页信息
     *
     * @param pageRequest 分页请求
     * @param principal   当前人
     * @param after       在什么时间之后的数据
     * @param type        类型
     *
     * @return 分页信息
     */
    Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type);

    /**
     * 通过唯一识别获取数据
     *
     * @param idEntity 唯一识别;
     *
     * @return 审计事件
     */
    AuditEvent get(StringIdEntity idEntity);

    /**
     * 创建审计事件
     *
     * @param map map 数据源
     *
     * @return 审计事件
     */
    default AuditEvent createAuditEvent(Map<String, Object> map) {
        Object timestamp = map.get(RestResult.DEFAULT_TIMESTAMP_NAME);

        Instant instant;
        if (timestamp instanceof Date) {
            Date date = Casts.cast(timestamp);
            instant = date.toInstant();
        } else if (timestamp instanceof Instant) {
            instant = Casts.cast(timestamp);
        } else if (timestamp instanceof Long){
            Long epochMilli = Casts.cast(timestamp);
            instant = Instant.ofEpochMilli(epochMilli);
        } else if (timestamp instanceof String) {
            String string = timestamp.toString();
            LocalDateTime localDateTime = LocalDateTime.parse(string);
            instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } else {
            throw new SystemException("找不到 " + RestResult.DEFAULT_TIMESTAMP_NAME + " 的数据转换支持");
        }

        String principal = map.get(PluginAuditEvent.PRINCIPAL_FIELD_NAME).toString();
        String type = map.get(PluginAuditEvent.TYPE_FIELD_NAME).toString();

        Map<String, Object> data = Casts.cast(map.getOrDefault(RestResult.DEFAULT_DATA_NAME, new LinkedHashMap<>()));
        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(instant, principal, type, data);

        Map<String, Object> principalMeta = Casts.cast(map.getOrDefault(PluginAuditEvent.PRINCIPAL_META_FIELD_NAME, new LinkedHashMap<>()));
        pluginAuditEvent.setPrincipalMeta(principalMeta);

        return pluginAuditEvent;
    }

}
