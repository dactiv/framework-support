package com.github.dactiv.framework.spring.security.audit.mongo;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.spring.security.audit.PluginAuditEventRepository;
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

    private MongoTemplate mongoTemplate;

    private SecurityProperties securityProperties;

    public MongoAuditEventRepository() {
    }

    public MongoAuditEventRepository(MongoTemplate mongoTemplate,
                                     SecurityProperties securityProperties) {

        this.mongoTemplate = mongoTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(event);

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

        //noinspection unchecked
        Map<String, Object> map = mongoTemplate.findById(id, Map.class, PluginAuditEvent.DEFAULT_INDEX_NAME);

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

        if (StringUtils.isNotEmpty(principal)) {
            criteria = criteria.and("principal").is(principal);
        }

        if (StringUtils.isNotEmpty(type)) {
            criteria = criteria.and("type").is(type);
        }

        if (Objects.nonNull(after)) {
            criteria = criteria.and("timestamp").gte(after);
        }

        return criteria;
    }
}
