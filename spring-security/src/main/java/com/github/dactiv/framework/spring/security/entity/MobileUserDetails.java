package com.github.dactiv.framework.spring.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.security.enumerate.UserStatus;

import java.io.Serial;

/**
 * 移动端的用户明细实现
 *
 * @author maurice
 */
public class MobileUserDetails extends SecurityUserDetails {

    @Serial
    private static final long serialVersionUID = -848955060608795664L;

    /**
     * 默认移动端登陆类型
     */
    public static final String DEFAULT_TYPE = "mobile";

    /**
     * 设备唯一识别
     */
    @JsonIgnore
    private String deviceIdentified;

    /**
     * 移动端用户明细实现
     */
    public MobileUserDetails() {
        setType(DEFAULT_TYPE);
    }

    /**
     * 移动端用户明细实现
     *
     * @param id               用户 id
     * @param username         登录账户
     * @param password         密码
     * @param deviceIdentified 设备唯一是被
     */
    public MobileUserDetails(Integer id, String username, String password, String deviceIdentified) {
        super(id, username, password, UserStatus.Enabled);
        this.deviceIdentified = deviceIdentified;
        setType(DEFAULT_TYPE);
    }

    /**
     * 获取设备唯一识别
     *
     * @return 唯一识别
     */
    public String getDeviceIdentified() {
        return deviceIdentified;
    }

    /**
     * 设置设备唯一识别
     *
     * @param deviceIdentified 设备唯一识别
     */
    public void setDeviceIdentified(String deviceIdentified) {
        this.deviceIdentified = deviceIdentified;
    }
}
