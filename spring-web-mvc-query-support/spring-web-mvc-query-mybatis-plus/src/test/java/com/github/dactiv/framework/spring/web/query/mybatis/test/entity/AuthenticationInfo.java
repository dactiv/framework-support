package com.github.dactiv.framework.spring.web.query.mybatis.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * <p>认证信息实体类</p>
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice
 * @since 2020-06-01 09:22:12
 */
@TableName("tb_authentication_info")
public class AuthenticationInfo implements NumberIdEntity<Integer> {

    public static final String DEFAULT_INDEX = "authentication-info";

    private static final long serialVersionUID = 5548079224380108843L;
    /**
     * 主键
     */
    @Id
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 用户 id
     */
    @NotNull
    private Integer userId;

    /**
     * 用户类型
     */
    @NotEmpty
    private String type;

    /**
     * ip 地址
     */
    @NotEmpty
    private String ip;

    /**
     * 设备名称
     */
    @NotEmpty
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> device;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String area;

    /**
     * 同步 es 状态：0.处理中，1.成功，99.失败
     */
    private Integer syncStatus = ExecuteStatus.Processing.getValue();

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 备注
     */
    private String remark;

    public AuthenticationInfo() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, String> getDevice() {
        return device;
    }

    public void setDevice(Map<String, String> device) {
        this.device = device;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}