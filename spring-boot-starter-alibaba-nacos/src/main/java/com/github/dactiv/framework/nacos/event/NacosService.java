package com.github.dactiv.framework.nacos.event;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;

import java.io.Serial;
import java.util.List;

/**
 * nacos 服务实体
 *
 * @author maurice.chen
 */
public class NacosService extends Service {

    @Serial
    private static final long serialVersionUID = -5952401177051610675L;

    /**
     * 服务实例集合
     */
    private List<Instance> instances;

    /**
     * nacos 服务实体
     */
    public NacosService() {

    }

    /**
     * 获取 nacos 服务实例集合
     *
     * @return nacos 服务实例集合
     */
    public List<Instance> getInstances() {
        return instances;
    }

    /**
     * 设置服务实例集合
     *
     * @param instances 服务实例集合
     */
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
}
