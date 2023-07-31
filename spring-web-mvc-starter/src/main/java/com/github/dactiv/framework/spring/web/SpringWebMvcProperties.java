package com.github.dactiv.framework.spring.web;


import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.RestResultErrorAttributes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;

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
    private List<String> filterViewBasePackages = new ArrayList<>();

    /**
     * 过滤属性的 id 头名称, "X-FILTER-RESULT-ID"
     */
    private String filterResultIdHeaderName = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_HEADER_NAME;

    /**
     * 过滤属性的 id 参数名称, 默认为 "filterResultId"
     */
    private String filterResultIdParamName = RestResponseBodyAdvice.DEFAULT_FILTER_RESULT_ID_PARAM_NAME;

    /**
     * 支持格式化的客户端集合，默认为 "SPRING_GATEWAY"
     */
    private List<String> restResultFormatSupportClients = RestResponseBodyAdvice.DEFAULT_SUPPORT_CLIENT;

    private boolean allRestResultFormat = false;

    /**
     * 是否使用 filter result 的 ObjectMapper 设置到 Casts工具类中, 默认为 true
     */
    private boolean useFilterResultObjectMapperToCastsClass = true;

    /**
     * 支持的异常抛出消息的类
     */
    private List<Class<? extends Exception>> supportException = RestResultErrorAttributes.DEFAULT_MESSAGE_EXCEPTION;

    /**
     * 支持的 http 响应状态
     */
    private List<HttpStatus> supportHttpStatus = RestResultErrorAttributes.DEFAULT_HTTP_STATUSES_MESSAGE;

    /**
     * Undertow 的 webSocketDeploymentBuffers 默认值
     */
    private int webSocketDeploymentBuffers = 2048;

    public SpringWebMvcProperties() {
    }

    /**
     * 获取需要扫描的包路径
     *
     * @return 需要扫描的包路径
     */
    public List<String> getFilterViewBasePackages() {
        return filterViewBasePackages;
    }

    /**
     * 设置需要扫描的包路径
     *
     * @param filterViewBasePackages 需要扫描的包路径
     */
    public void setFilterViewBasePackages(List<String> filterViewBasePackages) {
        this.filterViewBasePackages = filterViewBasePackages;
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
    public List<String> getRestResultFormatSupportClients() {
        return restResultFormatSupportClients;
    }

    /**
     * 设置可支持格式化的客户端信息
     *
     * @param restResultFormatSupportClients 客户端信息
     */
    public void setRestResultFormatSupportClients(List<String> restResultFormatSupportClients) {
        this.restResultFormatSupportClients = restResultFormatSupportClients;
    }

    /**
     * 是否支持客户端格式化
     *
     * @param client 客户端
     *
     * @return true 是，否则 false
     */
    public boolean isSupportClient(String client) {
        return restResultFormatSupportClients.contains(client);
    }

    /**
     * 是否使用 filter result 的 ObjectMapper设置到 Casts工具类中
     *
     * @return true 是，否则 false
     */
    public boolean isUseFilterResultObjectMapperToCastsClass() {
        return useFilterResultObjectMapperToCastsClass;
    }

    /**
     * 设置是否使用 filter result 的 ObjectMapper设置到 Casts工具类中
     *
     * @param useFilterResultObjectMapperToCastsClass true 是，否则 false
     */
    public void setUseFilterResultObjectMapperToCastsClass(boolean useFilterResultObjectMapperToCastsClass) {
        this.useFilterResultObjectMapperToCastsClass = useFilterResultObjectMapperToCastsClass;
    }

    /**
     * 获取 Undertow 的 webSocketDeploymentBuffers 默认值
     *
     * @return Undertow 的 webSocketDeploymentBuffers 默认值
     */
    public int getWebSocketDeploymentBuffers() {
        return webSocketDeploymentBuffers;
    }

    /**
     * 设置 Undertow 的 webSocketDeploymentBuffers 默认值
     *
     * @param webSocketDeploymentBuffers Undertow 的 webSocketDeploymentBuffers 默认值
     */
    public void setWebSocketDeploymentBuffers(int webSocketDeploymentBuffers) {
        this.webSocketDeploymentBuffers = webSocketDeploymentBuffers;
    }

    /**
     * 获取支持的异常抛出消息的类
     *
     * @return 支持的异常抛出消息的类
     */
    public List<Class<? extends Exception>> getSupportException() {
        return supportException;
    }

    /**
     * 设置支持的异常抛出消息的类
     *
     * @param supportException 支持的异常抛出消息的类
     */
    public void setSupportException(List<Class<? extends Exception>> supportException) {
        this.supportException = supportException;
    }

    /**
     * 获取支持的 http 响应状态
     *
     * @return 支持的 http 响应状态
     */
    public List<HttpStatus> getSupportHttpStatus() {
        return supportHttpStatus;
    }

    /**
     * 设置支持的 http 响应状态
     *
     * @param supportHttpStatus 支持的 http 响应状态
     */
    public void setSupportHttpStatus(List<HttpStatus> supportHttpStatus) {
        this.supportHttpStatus = supportHttpStatus;
    }

    public boolean isAllRestResultFormat() {
        return allRestResultFormat;
    }

    public void setAllRestResultFormat(boolean allRestResultFormat) {
        this.allRestResultFormat = allRestResultFormat;
    }
}
