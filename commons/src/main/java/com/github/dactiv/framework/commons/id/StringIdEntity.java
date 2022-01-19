package com.github.dactiv.framework.commons.id;

import java.util.Date;

/**
 * 字符串的 id 主键实体
 *
 * @author maurice.chen
 */
public class StringIdEntity extends IdEntity<String> {

    private static final long serialVersionUID = 6774769809276207267L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 创建一个字符串的 id 主键实体
     */
    public StringIdEntity() {
    }

    /**
     * 创建字符串的 id 主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public StringIdEntity(String id, Date creationTime) {
        super(id);
        this.creationTime = creationTime;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
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
     * 创建字符串的 id 主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public static StringIdEntity of(String id, Date creationTime) {
        return new StringIdEntity(id, creationTime);
    }
}
