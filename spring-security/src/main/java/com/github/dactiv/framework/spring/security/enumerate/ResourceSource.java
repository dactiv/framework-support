package com.github.dactiv.framework.spring.security.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameEnum;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
public enum ResourceSource implements NameEnum {

    /**
     * 前端
     */
    Front("前端"),
    /**
     * 管理后台
     */
    Console("管理后台"),
    /**
     * 用户中心
     */
    UserCenter("用户中心"),
    /**
     * 匿名用户
     */
    AnonymousUser("匿名用户"),
    /**
     * 系统
     */
    System("系统"),
    /**
     * 移动端
     */
    Mobile("移动端"),
    /**
     * 全部
     */
    All("全部");

    /**
     * 插件来源枚举
     *
     * @param name 中文名称
     */
    ResourceSource(String name) {
        this.name = name;
    }

    /**
     * 中文名称
     */
    private String name;

    @Override
    public String getName() {
        return name;
    }

}
