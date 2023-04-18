package com.github.dactiv.framework.spring.security.entity;

import com.github.dactiv.framework.mybatis.interceptor.audit.OperationDataTraceRecord;

import java.io.Serial;
import java.util.Date;
import java.util.Map;

/**
 * 用户明细操作数据留痕记录
 *
 * @author maurice.chen
 */
public class UserDetailsOperationDataTraceRecord extends OperationDataTraceRecord {

    @Serial
    private static final long serialVersionUID = 3211452737634539720L;

    /**
     * 当事人元数据
     */
    private Map<String, Object> principalMeta;

    /**
     * 关联业务 id
     */
    private String traceId;

    public UserDetailsOperationDataTraceRecord() {
    }

    public UserDetailsOperationDataTraceRecord(String id, Date creationTime) {
        super(id, creationTime);
    }

    /**
     * 获取当事人元数据
     *
     * @return 当事人元数据
     */
    public Map<String, Object> getPrincipalMeta() {
        return principalMeta;
    }

    /**
     * 设置当事人元数据
     *
     * @param principalMeta 当事人元数据
     */
    public void setPrincipalMeta(Map<String, Object> principalMeta) {
        this.principalMeta = principalMeta;
    }

    /**
     * 获取关联业务 id
     *
     * @return 关联业务 id
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置关联业务 id
     *
     * @param traceId 关联业务 id
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
