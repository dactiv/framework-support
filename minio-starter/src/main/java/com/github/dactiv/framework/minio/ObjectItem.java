package com.github.dactiv.framework.minio;

import io.minio.messages.Item;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 对象项类，用于在获取的内容 {@link Item} 时，能够序列化成 json 使用
 *
 * @author maurice.chen
 */
public class ObjectItem implements Serializable {

    private final Item item;

    public ObjectItem(Item item) {
        this.item = item;
    }

    public String getObjectName(){
        return item.objectName();
    }

    public String getEtag() {
        return item.etag();
    }

    public LocalDateTime getLastModified() {
        return item.lastModified().toLocalDateTime();
    }

    public long getSize() {
        return item.size();
    }

    public Map<String, String> getUserMetadata() {
        return item.userMetadata();
    }
}
