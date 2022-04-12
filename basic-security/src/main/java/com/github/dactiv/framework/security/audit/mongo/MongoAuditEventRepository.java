package com.github.dactiv.framework.security.audit.mongo;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * mongo 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class MongoAuditEventRepository implements PluginAuditEventRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoAuditEventRepository.class);

    private final MongoTemplate mongoTemplate;

    private final SecurityProperties securityProperties;

    private final List<String> ignorePrincipals;

    public MongoAuditEventRepository(MongoTemplate mongoTemplate,
                                     List<String> ignorePrincipals,
                                     SecurityProperties securityProperties) {

        this.mongoTemplate = mongoTemplate;
        this.ignorePrincipals = ignorePrincipals;
        this.securityProperties = securityProperties;
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(event);

        if (pluginAuditEvent.getPrincipal().equals(securityProperties.getUser().getName())) {
            return;
        }

        if (PluginAuditEvent.class.isAssignableFrom(event.getClass())) {
            pluginAuditEvent = Casts.cast(event);
        }

        try {

            if (!pluginAuditEvent.getPrincipal().equals(securityProperties.getUser().getName())) {
                event = mongoTemplate.save(pluginAuditEvent, PluginAuditEvent.DEFAULT_INDEX_NAME);
            }

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        Criteria criteria = createCriteria(principal, after, type);

        Query query = new Query(criteria).with(Sort.by(Sort.Order.desc("timestamp")));

        //noinspection rawtypes
        List<Map> result = mongoTemplate.find(query, Map.class, PluginAuditEvent.DEFAULT_INDEX_NAME);

        return result.stream().map(this::createPluginAuditEvent).collect(Collectors.toList());
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        Criteria criteria = createCriteria(principal, after, type);

        Query query = new Query(criteria)
                .with(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()))
                .with(Sort.by(Sort.Order.desc("timestamp")));

        //noinspection rawtypes
        List<Map> data = mongoTemplate.find(query, Map.class, PluginAuditEvent.DEFAULT_INDEX_NAME);

        return new Page<>(pageRequest, data.stream().map(this::createPluginAuditEvent).collect(Collectors.toList()));
    }

    @Override
    public AuditEvent get(Object id) {
        if (!StringIdEntity.class.isAssignableFrom(id.getClass())) {
            throw new SystemException("目标对象不是 StringIdEntity 对象");
        }

        StringIdEntity stringIdEntity = Casts.cast(id);
        //noinspection unchecked
        Map<String, Object> map = mongoTemplate.findById(stringIdEntity.getId(), Map.class, PluginAuditEvent.DEFAULT_INDEX_NAME);

        if (MapUtils.isNotEmpty(map)) {
            return createPluginAuditEvent(map);
        }

        return null;
    }

    /**
     * 创建插件审计事件
     *
     * @param map map 数据源
     *
     * @return 插件审计事件
     */
    public PluginAuditEvent createPluginAuditEvent(Map<String, Object> map) {
        AuditEvent auditEvent = createAuditEvent(map);

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(auditEvent);
        pluginAuditEvent.setId(map.get("_id").toString());

        return pluginAuditEvent;
    }

    /**
     * 创建查询条件
     *
     * @param principal 操作人
     * @param after     在什么时间之后的
     * @param type      类型
     *
     * @return 查询条件
     */
    private Criteria createCriteria(String principal, Instant after, String type) {

        Criteria criteria = new Criteria();

        if (StringUtils.isNotBlank(principal)) {
            criteria = criteria.and("principal").is(principal);
        }

        if (StringUtils.isNotBlank(type)) {
            criteria = criteria.and("type").is(type);
        }

        if (Objects.nonNull(after)) {
            criteria = criteria.and("timestamp").gte(after);
        }

        return criteria;
    }
}
