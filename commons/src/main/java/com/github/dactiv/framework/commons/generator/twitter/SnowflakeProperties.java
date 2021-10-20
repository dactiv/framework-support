/*
 * Copyright 2018 qianpen-group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dactiv.framework.commons.generator.twitter;


/**
 * twitter 的 snowflake id 生成算法配置信息
 *
 * @author maurice
 */
public class SnowflakeProperties {
    /**
     * 数据中心编号，最大支持数据中心节点数 0~31，一共 32 个
     */
    private long dataCenterId;
    /**
     * 服务编号（业务编号），共3位以字符串组成，000~999，共1000个
     */
    private String serviceId;
    /**
     * 工作机器编号，最大支持机器节点数 0~31，一共 32 个
     */
    private long workerId;

    /**
     * twitter 的 snowflake id 生成算法配置信息
     */
    public SnowflakeProperties() {

    }

    /**
     * twitter 的 snowflake id 生成算法配置信息
     *
     * @param dataCenterId 数据中心编号，最大支持数据中心节点数 0~31，一共 32 个
     * @param serviceId    服务编号（业务编号），共3位以字符串组成，000~999，共1000个
     * @param workerId     工作机器编号，最大支持机器节点数 0~31，一共 32 个
     */
    public SnowflakeProperties(long dataCenterId, String serviceId, long workerId) {
        this.dataCenterId = dataCenterId;
        this.serviceId = serviceId;
        this.workerId = workerId;
    }

    /**
     * 获取数据中心编号，最大支持数据中心节点数0~31，一共32个
     *
     * @return 数据中心编号
     */
    public long getDataCenterId() {
        return dataCenterId;
    }

    /**
     * 设置数据中心编号
     *
     * @param dataCenterId 数据中心编号，最大支持数据中心节点数0~31，一共32个
     */
    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获取服务编号（业务编号），共3位以字符串组成，000~999，共1000个
     *
     * @return 服务编号（业务编号）
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * 设置服务编号（业务编号）
     *
     * @param serviceId 服务编号（业务编号），共3位以字符串组成，000~999，共1000个
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 获取工作机器编号，最大支持机器节点数0~31，一共32个
     *
     * @return 工作机器编号
     */
    public long getWorkerId() {
        return workerId;
    }

    /**
     * 设置工作机器编号
     *
     * @param workerId 工作机器编号，最大支持机器节点数0~31，一共32个
     */
    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }
}
