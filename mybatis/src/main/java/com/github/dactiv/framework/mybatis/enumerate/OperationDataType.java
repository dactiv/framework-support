package com.github.dactiv.framework.mybatis.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import org.apache.ibatis.mapping.SqlCommandType;

public enum OperationDataType implements NameValueEnum<SqlCommandType> {

    /**
     * 新增
     */
    INSERT(SqlCommandType.INSERT, "新增"),

    /**
     * 更新
     */
    UPDATE(SqlCommandType.UPDATE, "更新"),

    /**
     * 删除
     */
    DELETE(SqlCommandType.DELETE, "删除"),

    ;

    OperationDataType(SqlCommandType value, String name) {
        this.value = value;
        this.name = name;
    }

    private final SqlCommandType value;

    private final String name;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public SqlCommandType getValue() {
        return value;
    }
}
