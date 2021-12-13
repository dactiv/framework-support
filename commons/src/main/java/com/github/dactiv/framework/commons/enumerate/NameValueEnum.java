package com.github.dactiv.framework.commons.enumerate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dactiv.framework.commons.jackson.deserializer.NameValueEnumDeserializer;

/**
 * key value 枚举， 针对键为 String，值为任意 Object 类型的枚举接口
 * 父类，该类用于在某些实体类中使用值存储，但值又有对应的名称使用。
 *
 * @param <V> 值类型
 *
 * @author maurice
 */
@JsonDeserialize(using = NameValueEnumDeserializer.class)
public interface NameValueEnum<V> extends NameEnum, ValueEnum<V> {

}
