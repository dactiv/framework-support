package com.github.dactiv.framework.security.entity;

import com.github.dactiv.framework.commons.CacheProperties;

/**
 * 带用户类型的用户明细实体
 *
 * @param <T> 主键 id 类型
 */
public interface TypeUserDetails<T> {

    /**
     * 获取用户主键 id
     *
     * @return 用户主键 id
     */
    T getUserId();

    /**
     * 设置用户主键 id
     *
     * @param userId 用户主键 id
     */
    void setUserId(T userId);

    /**
     * 获取登陆账户
     *
     * @return 登陆账户
     */
    String getUsername();

    /**
     * 设置登陆账户
     *
     * @param username 登陆账户
     */
    void setUsername(String username);

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    String getUserType();

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    void setUserType(String userType);

    /**
     * 设置用户信息
     *
     * @param userDetails 基础用户信息
     */
    default void setUserDetails(TypeUserDetails<T> userDetails) {
        this.setUserId(userDetails.getUserId());
        this.setUsername(userDetails.getUsername());
        this.setUserType(userDetails.getUserType());
    }

    /**
     * 获取唯一值
     *
     * @return 唯一值
     */
    default String toUniqueValue() {
        return getUserType() + CacheProperties.DEFAULT_SEPARATOR + getUserId() + CacheProperties.DEFAULT_SEPARATOR + getUsername();
    }
}
