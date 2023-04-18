package com.github.dactiv.framework.spring.security.audit;

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
     *
     * @return 操作数据留痕集合
     */
    List<OperationDataTraceRecord> find(String target, Date creationTime, Object entityId);

    /**
     * 根据目标值和实体 id 查询操作数据留痕分页
     *
     * @param pageRequest  分页请求
     * @param target 目标值
     * @param entityId 实体 id
     *
     * @return 操作数据留痕分页
     *
     */
    Page<OperationDataTraceRecord> findPage(PageRequest pageRequest,  Date creationTime, String target, Object entityId);

}
