package com.github.dactiv.framework.mybatis.plus.test.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.mybatis.plus.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.handler.NameValueEnumTypeHandler;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@TableName(value = "tb_all_type_entity", autoResultMap = true)
public class AllTypeEntity implements BasicIdentification<Integer> {

    private static final long serialVersionUID = 5548079224380108843L;

    private Integer id;

    @NotEmpty
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, String> device;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<StringIdEntity> entities;

    @TableField(typeHandler = NameValueEnumTypeHandler.class)
    private DisabledOrEnabled status = DisabledOrEnabled.Disabled;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ExecuteStatus> executes = List.of(ExecuteStatus.Processing);

    public AllTypeEntity() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getDevice() {
        return device;
    }

    public void setDevice(Map<String, String> device) {
        this.device = device;
    }

    public List<StringIdEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<StringIdEntity> entities) {
        this.entities = entities;
    }

    public DisabledOrEnabled getStatus() {
        return status;
    }

    public void setStatus(DisabledOrEnabled status) {
        this.status = status;
    }

    public List<ExecuteStatus> getExecutes() {
        return executes;
    }

    public void setExecutes(List<ExecuteStatus> executes) {
        this.executes = executes;
    }
}