package com.github.dactiv.framework.commons.id.number;

import com.github.dactiv.framework.commons.id.IdEntity;

import java.util.Date;

/**
 * 整型主键实体
 *
 * @author maurice.chen
 */
public class IntegerIdEntity extends IdEntity<Integer> implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 6284036190187423322L;

    /**
     * 创建时间
     */
    private Date creationTime;

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
