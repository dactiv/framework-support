package com.github.dactiv.framework.spring.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

/**
 * 支持分页查询审计事件的仓库实现
 *
 * @author maurice.chen
 */
public interface PageAuditEventRepository extends AuditEventRepository {

    /**
     * 获取分页信息
     *
     * @param pageable 分页请求
     * @param principal 当前人
     * @param after 在什么时间之后的
     * @param type 类型
     *
     * @return 分页信息
     */
    Page<AuditEvent> findPage(Pageable pageable,String principal, Instant after, String type);
}
