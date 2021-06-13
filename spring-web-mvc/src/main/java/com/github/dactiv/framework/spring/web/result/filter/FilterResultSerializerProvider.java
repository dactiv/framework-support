package com.github.dactiv.framework.spring.web.result.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

import java.io.IOException;

/**
 * 过滤结果集序列话供应者实现，由于默认的序列话供应者对于 JsonSerializer 缓存机制，无法执行新的序列化过程，所以必须要重写
 *
 * @author maurice.chen
 *
 */
public class FilterResultSerializerProvider extends DefaultSerializerProvider {

    public FilterResultSerializerProvider() { super(); }

    public FilterResultSerializerProvider(FilterResultSerializerProvider src) { super(src); }

    protected FilterResultSerializerProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
        super(src, config, f);
    }

    @Override
    public DefaultSerializerProvider copy()
    {
        if (getClass() != FilterResultSerializerProvider.class) {
            return super.copy();
        }
        return new FilterResultSerializerProvider(this);
    }

    @Override
    public FilterResultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
        return new FilterResultSerializerProvider(this, config, jsf);
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value) throws IOException {
        super.serializeValue(gen, value);
        flushCachedSerializers();
    }

    @Override
    public void serializeValue(JsonGenerator gen, Object value, JavaType rootType) throws IOException {
        super.serializeValue(gen, value, rootType);
        flushCachedSerializers();
    }
}
