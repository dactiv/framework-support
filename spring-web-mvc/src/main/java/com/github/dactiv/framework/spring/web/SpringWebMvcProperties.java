package com.github.dactiv.framework.spring.web;


import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * spring web 扩展支持的配置类
 *
 * @author maurice.chen
 */
@ConfigurationProperties("dactiv.spring.web.mvc")
public class SpringWebMvcProperties {

    /**
     * 需要扫描的包路径，用于指定哪个包下面的类引入了 filter 注解，通过该配置自动添加 jackson filter
     */
    private List<String> basePackages = new ArrayList<>();

    /**
     * 过滤属性的 id 头名称
     */
    private String filterResultIdHeaderName = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_HEADER_NAME;

    /**
     * 过滤属性的 id 参数名称
     */
    private String filterResultIdParamName = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_PARAM_NAME;

    /**
     * 支持格式化的客户端集合
     */
    private List<String> supportClients = RestResponseBodyAdvice.DEFAULT_SUPPORT_CLIENT;

    public SpringWebMvcProperties() {
    }

    /**
     * 获取需要扫描的包路径
     *
     * @return 需要扫描的包路径
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

    /**
     * 设置需要扫描的包路径
     *
     * @param basePackages 需要扫描的包路径
     */
    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 获取过滤属性的 id 头名称
     *
     * @return 过滤属性的 id 头名称
     */
    public String getFilterResultIdHeaderName() {
        return filterResultIdHeaderName;
    }

    /**
     * 设置过滤属性的 id 头名称
     *
     * @param filterResultIdHeaderName 过滤属性的 id 头名称
     */
    public void setFilterResultIdHeaderName(String filterResultIdHeaderName) {
        this.filterResultIdHeaderName = filterResultIdHeaderName;
    }

    /**
     * 获取过滤属性的 id 参数名称
     *
     * @return 过滤属性的 id 参数名称
     */
    public String getFilterResultIdParamName() {
        return filterResultIdParamName;
    }

    /**
     * 设置过滤属性的 id 参数名称
     *
     * @param filterResultIdParamName 过滤属性的 id 参数名称
     */
    public void setFilterResultIdParamName(String filterResultIdParamName) {
        this.filterResultIdParamName = filterResultIdParamName;
    }

    /**
     * 获取可支持格式化的客户端信息
     *
     * @return 可支持格式化的客户端信息
     */
    public List<String> getSupportClients() {
        return supportClients;
    }

    /**
     * 设置可支持格式化的客户端信息
     *
     * @param supportClients 客户端信息
     */
    public void setSupportClients(List<String> supportClients) {
        this.supportClients = supportClients;
    }

    /**
     * 是否支持客户端格式化
     *
     * @param client 客户端
     *
     * @return true 是，否则 false
     */
    public boolean isSupportClient(String client) {
        return supportClients.contains(client);
    }
}
