package com.github.dactiv.framework.security.entity;

import java.io.Serializable;

/**
 * 基础用户信息
 *
 * @author maurice.chen
 */
public class BasicUserDetails<T> implements Serializable, TypeUserDetails<T> {

    /**
     * 用户 id 字段名称
     */
    public static final String USER_ID_FIELD_NAME = "userId";

    /**
     * 用户类型字段名称
     */
    public static final String USER_TYPE_FIELD_NAME = "userType";

    /**
     * 登陆账户字段名称
     */
    public static final String USERNAME_FIELD_NAME = "username";

    /**
     * 用户 id
     */
    private T userId;

    /**
     * 登陆账户
     */
    private String username;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 创建一个新的基础用户信息
     *
     * @param userId 用户 id
     * @param username 登陆账号
     * @param userType 用户类型
     */
    public BasicUserDetails(T userId, String username, String userType) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
    }

    /**
     * 创建一个新的基础用户信息
     */
    public BasicUserDetails() {
    }

    /**
     * 获取用户主键 id
     *
     * @return 用户主键 id
     */
    public T getUserId() {
        return userId;
    }

    /**
     * 设置用户主键 id
     *
     * @param userId 用户主键 id
     */
    public void setUserId(T userId) {
        this.userId = userId;
    }

    /**
     * 获取登陆账户
     *
     * @return 登陆账户
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登陆账户
     *
     * @param username 登陆账户
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 创建一个新的基础用户信息
     *
     * @param userId 用户 id
     * @param username 登陆账号
     * @param userType 用户类型
     *
     * @return 新的基础用户信息
     */
    public static <T> BasicUserDetails<T> of(T userId, String username, String userType) {
        return new BasicUserDetails<>(userId, username, userType);
    }
}
