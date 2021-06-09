package com.github.dactiv.framework.spring.web.test.result.entity;

import com.github.dactiv.framework.spring.web.result.filter.annotation.ExcludeProperties;

import java.util.LinkedList;
import java.util.List;

@ExcludeProperties(value = "unity", properties = {"creationTime","sex", "age"})
public class TestUser {

    public static final String[] DEFAULT_FILTER_PROPERTIES = {"id", "creationTime","sex", "age", "username"};

    private Integer id;

    private Integer creationTime;

    private String nickname;

    @ExcludeProperties.Exclude("unity")
    private String username;

    private String sex;

    private Integer age;

    private TestUser noFilterPropertiesUser;

    @ExcludeProperties(value = "unity", properties = {"id", "creationTime","age"})
    private TestUser filterPropertiesUser;

    @ExcludeProperties(value = "unity", properties = {"id", "creationTime","age"}, filterClassType = true)
    private TestUser classTypeFilterPropertiesUser;


    @ExcludeProperties(value = "unity", properties = {"id", "creationTime","sex"})
    private List<TestUser> userList = new LinkedList<>();

    @ExcludeProperties.Exclude("unity")
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

    public TestUser getNoFilterPropertiesUser() {
        return noFilterPropertiesUser;
    }

    public void setNoFilterPropertiesUser(TestUser noFilterPropertiesUser) {
        this.noFilterPropertiesUser = noFilterPropertiesUser;
    }

    public TestUser getFilterPropertiesUser() {
        return filterPropertiesUser;
    }

    public void setFilterPropertiesUser(TestUser filterPropertiesUser) {
        this.filterPropertiesUser = filterPropertiesUser;
    }

    public TestUser getClassTypeFilterPropertiesUser() {
        return classTypeFilterPropertiesUser;
    }

    public void setClassTypeFilterPropertiesUser(TestUser classTypeFilterPropertiesUser) {
        this.classTypeFilterPropertiesUser = classTypeFilterPropertiesUser;
    }

    public List<TestUser> getUserList() {
        return userList;
    }

    public void setUserList(List<TestUser> userList) {
        this.userList = userList;
    }
}
