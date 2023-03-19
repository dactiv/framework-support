package com.github.dactiv.framework.commons.minio;

import com.github.dactiv.framework.commons.TimeProperties;

import java.io.Serial;

/**
 * 可过期的桶
 *
 * @author maurice.chen
 */
public class ExpirableBucket extends Bucket {

    @Serial
    private static final long serialVersionUID = -2648131524041207239L;

    /**
     * 过期时间
     */
    private TimeProperties expirationTime;

    public TimeProperties getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(TimeProperties expirationTime) {
        this.expirationTime = expirationTime;
    }
}
