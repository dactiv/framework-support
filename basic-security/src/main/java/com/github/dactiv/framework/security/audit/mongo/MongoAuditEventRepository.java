package com.github.dactiv.framework.security.audit.mongo;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import com.github.dactiv.framework.security.audit.elasticsearch.index.IndexGenerator;
import com.github.dactiv.framework.security.audit.elasticsearch.index.support.DateIndexGenerator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import java.sql.Date;
import java.time.Instant;
import java.util.LinkedList;
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

    public static final String DEFAULT_COLLECTION_NAME = "col_http_request_audit_event";

    public static final String DEFAULT_ID_FIELD = "_id";

    private final MongoTemplate mongoTemplate;

    private final List<String> ignorePrincipals;

    private final IndexGenerator indexGenerator;

    public MongoAuditEventRepository(MongoTemplate mongoTemplate,
                                     String indexName,
                                     List<String> ignorePrincipals) {

        this.mongoTemplate = mongoTemplate;
        this.ignorePrincipals = ignorePrincipals;

        this.indexGenerator = new DateIndexGenerator(
                indexName,
                Casts.UNDERSCORE,
                List.of(RestResult.DEFAULT_TIMESTAMP_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME)
        );
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(event);

        if (ignorePrincipals.contains(pluginAuditEvent.getPrincipal())) {
            return ;
        }

        if (PluginAuditEvent.class.isAssignableFrom(event.getClass())) {
            pluginAuditEvent = Casts.cast(event);
        }

        try {
            String index = indexGenerator.generateIndex(pluginAuditEvent).toLowerCase();
            mongoTemplate.save(pluginAuditEvent, index);
        } catch (Exception e) {
            LOGGER.error("新增 mongo " + event.getPrincipal() + " 审计事件出现异常", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Assert.notNull(after, "查询 mongo 审计数据时 after 参数不能为空");

        Criteria criteria = createCriteria(principal, after, type);

        Query query = new Query(criteria).with(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)));

        return findData(getCollectionName(after), query);
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        Assert.notNull(after, "查询 mongo 审计f分页数据时 after 参数不能为空");

        Criteria criteria = createCriteria(principal, after, type);

        Query query = new Query(criteria)
                .with(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()))
                .with(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)));

        List<AuditEvent> data = findData(getCollectionName(after), query);

        return new Page<>(pageRequest, data);
    }

    private List<AuditEvent> findData(String index, Query query) {
        List<AuditEvent> content = new LinkedList<>();
        try {
            List<Map<String, Object>> data = Casts.cast(mongoTemplate.find(query, Map.class, index));
            content = data.stream().map(this::createAuditEvent).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("查询集合 [" + index + "] 出现错误", e);
        }

        return content;
    }

    @Override
    public AuditEvent get(Object id) {
        if (!StringIdEntity.class.isAssignableFrom(id.getClass())) {
            throw new SystemException("目标对象不是 StringIdEntity 对象");
        }

        StringIdEntity stringIdEntity = Casts.cast(id);
        String index = indexGenerator.generateIndex(stringIdEntity).toLowerCase();

        try {
            Map<String, Object> map = Casts.cast(mongoTemplate.findById(stringIdEntity.getId(), Map.class, index));
            if (MapUtils.isNotEmpty(map)) {
                return createAuditEvent(map);
            }
        } catch (Exception e) {
            LOGGER.error("查询集合 [" + index + "] 出现错误", e);
        }

        return null;
    }

    @Override
    public AuditEvent createAuditEvent(Map<String, Object> map) {
        PluginAuditEvent pluginAuditEvent = Casts.cast(PluginAuditEventRepository.super.createAuditEvent(map));
        pluginAuditEvent.setId(map.get(DEFAULT_ID_FIELD).toString());

        return pluginAuditEvent;
    }

    public String getCollectionName(Instant instant) {
        StringIdEntity id = new StringIdEntity();
        id.setCreationTime(Date.from(instant));
        return indexGenerator.generateIndex(id).toLowerCase();
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
            criteria = criteria.and(PluginAuditEvent.PRINCIPAL_FIELD_NAME).is(principal);
        }

        if (StringUtils.isNotBlank(type)) {
            criteria = criteria.and(PluginAuditEvent.TYPE_FIELD_NAME).is(type);
        }

        if (Objects.nonNull(after)) {
            criteria = criteria.and(RestResult.DEFAULT_TIMESTAMP_NAME).gte(Date.from(after));
        }

        return criteria;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
