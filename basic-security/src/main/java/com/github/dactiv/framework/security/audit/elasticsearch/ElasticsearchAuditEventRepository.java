package com.github.dactiv.framework.security.audit.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.StringIdEntity;
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
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;

import java.time.Instant;
import java.util.*;

/**
 * es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository implements PluginAuditEventRepository {

    public static final String DEFAULT_INDEX_NAME = "audit-event";

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private final ElasticsearchOperations elasticsearchOperations;

    private final SecurityProperties securityProperties;

    private final List<String> ignorePrincipals;

    private final IndexGenerator indexGenerator;

    public ElasticsearchAuditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                             List<String> ignorePrincipals,
                                             SecurityProperties securityProperties) {

        this.elasticsearchOperations = elasticsearchOperations;
        this.ignorePrincipals = ignorePrincipals;
        this.securityProperties = securityProperties;

        this.indexGenerator = new DateIndexGenerator(
                DEFAULT_INDEX_NAME,
                RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE,
                RestResult.DEFAULT_TIMESTAMP_NAME
        );
    }

    @Override
    public void add(AuditEvent event) {

        PluginAuditEvent pluginAuditEvent = new PluginAuditEvent(event);

        if (!validPrincipal(pluginAuditEvent.getPrincipal(), securityProperties.getUser().getName(), ignorePrincipals)) {
            return ;
        }

        if (PluginAuditEvent.class.isAssignableFrom(event.getClass())) {
            pluginAuditEvent = Casts.cast(event);
        }

        try {

            String index = indexGenerator.generateIndex(pluginAuditEvent).toLowerCase();

            IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder()
                    .withId(pluginAuditEvent.getId())
                    .withObject(pluginAuditEvent);

            elasticsearchOperations.index(indexQueryBuilder.build(), IndexCoordinates.of(index));

        } catch (Exception e) {
            LOGGER.error("新增" + event.getPrincipal() + "审计事件失败", e);
        }

    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        String index = getIndex(after, principal, type);

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> createQueryBuilder(q, after, type, principal))
                .withSort(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)));

        List<PluginAuditEvent> result = elasticsearchOperations
                .search(builder.build(), Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(this::createPluginAuditEvent)
                .toList();

        return new LinkedList<>(result);
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        String index = getIndex(after, principal, type);

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> createQueryBuilder(q, after, type, principal))
                .withSort(Sort.by(Sort.Order.desc(RestResult.DEFAULT_TIMESTAMP_NAME)))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()));

        List<PluginAuditEvent> content = elasticsearchOperations
                .search(builder.build(), Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(this::createPluginAuditEvent)
                .toList();

        return new Page<>(pageRequest, new ArrayList<>(content));
    }

    @Override
    public AuditEvent get(Object target) {

        if (!StringIdEntity.class.isAssignableFrom(target.getClass())) {
            throw new SystemException("目标对象不是 StringIdEntity 对象");
        }

        StringIdEntity stringIdEntity = Casts.cast(target);

        //noinspection unchecked
        Map<String, Object> map = elasticsearchOperations.get(
                stringIdEntity.getId(),
                Map.class,
                IndexCoordinates.of(indexGenerator.generateIndex(stringIdEntity))
        );

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
        pluginAuditEvent.setId(map.get(IdEntity.ID_FIELD_NAME).toString());

        return pluginAuditEvent;
    }

    /**
     * 创建查询条件
     *
     * @param after     在什么时间之后的
     * @param type      类型
     * @param principal 操作人
     *
     * @return 查询条件
     */
    private ObjectBuilder<Query> createQueryBuilder(Query.Builder builder, Instant after, String type, String principal) {
        List<Query> queryList = new LinkedList<>();

        if (StringUtils.isNotBlank(type)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(PluginAuditEvent.TYPE_FIELD_NAME).value(type))));
        }

        if (Objects.nonNull(after)) {
            queryList.add(Query.of(q -> q.range(r -> r.field(RestResult.DEFAULT_TIMESTAMP_NAME).gte(JsonData.of(after)))));
        }

        if (StringUtils.isNotBlank(principal)) {
            queryList.add(Query.of(q -> q.term(t -> t.field(PluginAuditEvent.PRINCIPAL_FIELD_NAME).value(principal))));
        }

        return builder.bool(t -> t.must(queryList));
    }

    /**
     * 获取当前 index
     *
     * @param after     在什么时间之后
     * @param type      类型
     * @param principal 操作人
     *
     * @return index 信息
     */
    private String getIndex(Instant after, String type, String principal) {
        try {

            AuditEvent auditEvent = new AuditEvent(
                    after,
                    StringUtils.defaultString(principal, StringUtils.EMPTY),
                    StringUtils.defaultString(type, StringUtils.EMPTY),
                    new LinkedHashMap<>()
            );

            return indexGenerator.generateIndex(auditEvent).toLowerCase();

        } catch (Exception e) {
            return DEFAULT_INDEX_NAME + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE + ProxyFactoryBean.GLOBAL_SUFFIX;
        }
    }
}
