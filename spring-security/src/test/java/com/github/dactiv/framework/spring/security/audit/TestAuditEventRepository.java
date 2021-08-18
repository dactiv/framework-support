package com.github.dactiv.framework.spring.security.audit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest
public class TestAuditEventRepository {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    public void test() {

        int before = auditEventRepository.find("admin", null, null).size();

        auditEventRepository.add(new AuditEvent(Instant.now(), "admin", "test", new LinkedHashMap<>()));

        List<AuditEvent> auditEvents = auditEventRepository.find("admin", null, null);

        Assertions.assertEquals(before + 1, auditEvents.size());

    }


}
