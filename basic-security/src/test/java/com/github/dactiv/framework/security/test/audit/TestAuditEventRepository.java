package com.github.dactiv.framework.security.test.audit;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.audit.PluginAuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 审计事件仓库单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
public class TestAuditEventRepository {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    public void test() {

        int before = auditEventRepository.find("admin", null, null).size();

        auditEventRepository.add(new PluginAuditEvent(Instant.now(), "admin", "test", new LinkedHashMap<>()));

        List<AuditEvent> auditEvents = auditEventRepository.find("admin", null, null);

        Assertions.assertEquals(before + 1, auditEvents.size());

        if (PluginAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass())) {

            PluginAuditEvent target = Casts.cast(auditEvents.iterator().next());

            PluginAuditEventRepository pluginAuditEventRepository = (PluginAuditEventRepository) auditEventRepository;

            AuditEvent event = pluginAuditEventRepository.get(target.getId());

            Assertions.assertEquals(event.getPrincipal(), target.getPrincipal());
            Assertions.assertEquals(event.getType(), target.getType());
            Assertions.assertEquals(event.getData().size(), target.getData().size());
            Assertions.assertEquals(event.getTimestamp(), target.getTimestamp());

        }

    }


}
