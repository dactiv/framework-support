package com.github.dactiv.framework.spring.web.test.result.entity;

import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;

import java.util.Date;
import java.util.UUID;

public class UserDetail {

    private Integer id = 1;

    @Exclude("unity")
    private Date creationTime = new Date();

    private String ip = "127.0.0.1";

    private DisabledOrEnabled status = DisabledOrEnabled.Enabled;

    public UserDetail() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public DisabledOrEnabled getStatus() {
        return status;
    }

    public void setStatus(DisabledOrEnabled status) {
        this.status = status;
    }

    public String getUuid() {
        return UUID.randomUUID().toString();
    }
}
