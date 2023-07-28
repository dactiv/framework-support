package com.github.dactiv.framework.commons.id.number;

import com.github.dactiv.framework.commons.id.IdEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 整型主键实体
 *
 * @author maurice.chen
 */
public class IntegerIdEntity extends IdEntity<Integer> implements NumberIdEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 6284036190187423322L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 创建一个整型主键实体
     */
    public IntegerIdEntity() {
    }

    /**
     * 创建一个整型主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public IntegerIdEntity(Integer id, Date creationTime) {
        super(id);
        this.creationTime = creationTime;
    }

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

    /**
     * 创建一个整型主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public static IntegerIdEntity of(Integer id, Date creationTime) {
        return new IntegerIdEntity(id, creationTime);
    }
}
