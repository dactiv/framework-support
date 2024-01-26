package com.github.dactiv.framework.security.test.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 审计事件仓库单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
@ActiveProfiles("elasticsearch")
public class TestElasticsearchAuditEventRepository {

    @Autowired
    private ElasticsearchAuditEventRepository auditEventRepository;

    @Test
    public void test() {
        Instant instant = Instant.now();

        int before = auditEventRepository.find("admin", instant, null).size();

        auditEventRepository.add(new PluginAuditEvent("admin", "test", Map.of("d", 1, "xx",3,"test","tests", "data", Map.of("date", new Date()))));

        List<AuditEvent> auditEvents = auditEventRepository.find("admin", instant, null);

        Assertions.assertEquals(before + 1, auditEvents.size());

        PluginAuditEvent target = Casts.cast(auditEvents.iterator().next());

        StringIdEntity id = new StringIdEntity();
        id.setId(target.getId());
        id.setCreationTime(Date.from(target.getTimestamp()));

        AuditEvent event = auditEventRepository.get(id);

        Assertions.assertEquals(event.getPrincipal(), target.getPrincipal());
        Assertions.assertEquals(event.getType(), target.getType());
        Assertions.assertEquals(event.getData().size(), target.getData().size());
        Assertions.assertEquals(event.getTimestamp(), target.getTimestamp());

        auditEventRepository
                .getElasticsearchTemplate()
                .indexOps(IndexCoordinates.of(auditEventRepository.getIndexName(instant)))
                .delete();

    }


}
