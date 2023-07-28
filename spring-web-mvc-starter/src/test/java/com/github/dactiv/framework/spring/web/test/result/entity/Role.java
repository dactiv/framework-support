package com.github.dactiv.framework.spring.web.test.result.entity;

import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;

import java.util.Date;
import java.util.UUID;

public class Role {

    private Integer id = 1;

    @Exclude("unity")
    private Date creationTime = new Date();

    private String name = "maurice.chen";

    private String authority = "admin";

    public Role() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getUuid() {
        return UUID.randomUUID().toString();
    }
}
