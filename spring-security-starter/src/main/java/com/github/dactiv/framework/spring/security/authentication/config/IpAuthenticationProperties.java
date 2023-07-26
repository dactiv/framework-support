package com.github.dactiv.framework.spring.security.authentication.config;

import java.util.LinkedList;
import java.util.List;

/**
 * ip 认证配置
 *
 * @author maurice.chen
 */
public class IpAuthenticationProperties {

    /**
     * 开放的 url
     */
    private String url;

    /**
     * 对应开放的 ip
     */
    private List<String> ips = new LinkedList<>();

    public IpAuthenticationProperties() {
    }

    /**
     * 获取开放的 url
     *
     * @return 开放的 url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置开放的 url
     *
     * @param url 开放的 url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取对应的 ip
     *
     * @return 对应的 ip 集合
     */
    public List<String> getIps() {
        return ips;
    }

    /**
     * 设置对应的 ip
     *
     * @param ips 对应的 ip 集合
     */
    public void setIps(List<String> ips) {
        this.ips = ips;
    }
}
