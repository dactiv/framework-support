package com.github.dactiv.framework.crypto.access;



import java.io.Serializable;

/**
 * 访问加解密条件
 *
 * @author maurice
 */
public class AccessCryptoPredicate implements Serializable {

    
    private static final long serialVersionUID = 5801688557790146889L;
    /**
     * 名称
     */
    private String name;
    /**
     * spring el 值
     */
    private String value;
    /**
     * 备注
     */
    private String remark;

    /**
     * 访问加解密条件
     */
    public AccessCryptoPredicate() {
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 spring el 值
     *
     * @return spring el 值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置 spring el 值
     *
     * @param value spring el 值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
