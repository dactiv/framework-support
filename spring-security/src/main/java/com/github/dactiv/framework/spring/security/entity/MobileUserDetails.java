package com.github.dactiv.framework.spring.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import nl.basjes.parse.useragent.UserAgent;

/**
 * 移动端的用户明细实现
 *
 * @author maurice
 */
public class MobileUserDetails extends SecurityUserDetails {

    private static final long serialVersionUID = -848955060608795664L;

    public static final String DEFAULT_TYPE = "mobile";

    /**
     * 设备唯一识别
     */
    @JsonIgnore
    private String deviceIdentified;

    /**
     * 设备
     */
    @JsonIgnore
    private UserAgent device;

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
     * @param device           设备
     */
    public MobileUserDetails(Integer id, String username, String password, String deviceIdentified, UserAgent device) {
        super(id, username, password, UserStatus.Enabled);
        this.deviceIdentified = deviceIdentified;
        this.device = device;
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
     * 获取设备
     *
     * @return 设备
     */
    public UserAgent getDevice() {
        return device;
    }

    /**
     * 设置设备唯一识别
     *
     * @param deviceIdentified 设备唯一识别
     */
    public void setDeviceIdentified(String deviceIdentified) {
        this.deviceIdentified = deviceIdentified;
    }

    /**
     * 获取设备信息
     *
     * @param device 设备信息
     */
    public void setDevice(UserAgent device) {
        this.device = device;
    }
}
