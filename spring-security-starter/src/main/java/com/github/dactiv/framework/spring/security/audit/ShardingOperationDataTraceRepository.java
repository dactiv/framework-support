package com.github.dactiv.framework.spring.security.audit;

import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.framework.mybatis.plus.audit.EntityIdOperationDataTraceRepository;

import java.util.Date;
import java.util.List;

/**
 * 分库存储的操作数据留痕仓库
 *
 * @author maurice.chen
 */
public interface ShardingOperationDataTraceRepository extends EntityIdOperationDataTraceRepository {

    /**
     * 根据目标值和实体 id 查询操作数据留痕集合
     *
     * @param target 目标值
     * @param entityId 实体 id
     * @param auditType 审计类型
     * @param principal 操作人
     *
     * @return 操作数据留痕集合
     */
    List<OperationDataTraceRecord> find(String target, Date creationTime, Object entityId, String auditType, String principal);

    /**
     * 根据目标值和实体 id 查询操作数据留痕分页
     *
     * @param pageRequest  分页请求
     * @param target 目标值
     * @param entityId 实体 id
     * @param auditType 审计类型
     * @param principal 操作人
     *
     * @return 操作数据留痕分页
     *
     */
    Page<OperationDataTraceRecord> findPage(PageRequest pageRequest,  Date creationTime, String target, Object entityId, String auditType, String principal);

    /**
     * 获取操作数据留痕
     *
     * @param idEntity 唯一识别
     *
     * @return 操作数据留痕
     */
    OperationDataTraceRecord get(StringIdEntity idEntity);

}
