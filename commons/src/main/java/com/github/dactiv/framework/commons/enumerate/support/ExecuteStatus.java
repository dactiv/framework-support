package com.github.dactiv.framework.commons.enumerate.support;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;

import java.util.Date;

/**
 * 执行状态枚举
 *
 * @author maurice
 */
public enum ExecuteStatus implements NameValueEnum<Integer> {

    /**
     * 执行中
     */
    Processing("执行中", 0),

    /**
     * 执行成功
     */
    Success("执行成功", 1),

    /**
     * 执行失败
     */
    Failure("执行失败", 99);

    /**
     * 执行状态枚举
     *
     * @param name  名称
     * @param value 值
     */
    ExecuteStatus(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    private final String name;

    private final Integer value;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 成功设置值
     *
     * @param body 数据体
     * @param message 消息
     */
    public static void success(Body body, String message) {
        body.setExecuteStatus(ExecuteStatus.Success);
        body.setRemark(message);
        body.setSuccessTime(new Date());
    }

    /**
     * 失败设置值
     *
     * @param body 数据体
     * @param message 消息
     */
    public static void failure(Body body, String message) {
        body.setExecuteStatus(ExecuteStatus.Failure);
        body.setRemark(message);
    }

    /**
     * 执行状态数据体
     *
     * @author maurice.chen
     */
    public interface Body {
        /**
         * 设置备注
         *
         * @param remark 备注
         */
        void setRemark(String remark);

        /**
         * 设置成功时间
         *
         * @param successTime 成功时间
         */
        void setSuccessTime(Date successTime);

        /**
         * 设置状态
         *
         * @param status 状态
         */
        void setExecuteStatus(ExecuteStatus status);
    }
}
