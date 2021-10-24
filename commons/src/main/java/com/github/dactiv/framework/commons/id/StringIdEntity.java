package com.github.dactiv.framework.commons.id;

import java.util.Date;

/**
 * 字符串的 id 主机实体
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
     * 获取创建时间
     *
     * @return
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
}
