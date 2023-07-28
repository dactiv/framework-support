package com.github.dactiv.framework.spring.web.test.result.entity;

import com.github.dactiv.framework.spring.web.result.filter.annotation.Exclude;
import com.github.dactiv.framework.spring.web.result.filter.annotation.view.ExcludeView;

import java.util.LinkedList;
import java.util.List;

@ExcludeView(value = "unity", properties = {"creationTime", "sex", "age"})
public class User {

    private Integer id = 1;

    private Integer creationTime = 1;

    private String nickname = "咏春.叶问";

    @Exclude("unity")
    private String username = "maurice.chen";

    private String sex = "male";

    private Integer age = 27;

    @ExcludeView(value = "unity", properties = {"creationTime"})
    private UserDetail userDetail = new UserDetail();

    private List<Role> roles = new LinkedList<>();

    public User() {
    }

    @Exclude("unity")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Integer creationTime) {
        this.creationTime = creationTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void generateRole(int count) {
        for (int i = 0; i < count; i++) {
            roles.add(new Role());
        }
    }
}
