package com.github.dactiv.framework.crypto.access.token;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.ExpirationToken;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 简单的可过期的 token 实现
 *
 * @author maurice
 */
public class SimpleExpirationToken extends SimpleToken implements ExpirationToken {

    @Serial
    private static final long serialVersionUID = -2524113941584019855L;

    /**
     * 创建时间
     */
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedTime = creationTime;

    /**
     * 超时时间
     */
    private TimeProperties maxInactiveInterval = new TimeProperties(DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS, TimeUnit.SECONDS);

    /**
     * 时间超时时间
     */
    private LocalDateTime expirationTime = lastAccessedTime.plus(maxInactiveInterval.getValue(), maxInactiveInterval.getUnit().toChronoUnit());

    /**
     * 超时的 token
     *
     * @param token               访问 token
     * @param maxInactiveInterval 最大超时时间
     */
    public SimpleExpirationToken(AccessToken token, TimeProperties maxInactiveInterval) {
        super(token.getType(), token.getToken(), token.getName(), token.getKey());
        this.maxInactiveInterval = maxInactiveInterval;
        this.expirationTime = creationTime.plus(maxInactiveInterval.getValue(), maxInactiveInterval.getUnit().toChronoUnit());
    }

    /**
     * 超时的 token
     */
    public SimpleExpirationToken() {
    }

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public LocalDateTime getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public void setLastAccessedTime(LocalDateTime lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
        this.expirationTime = lastAccessedTime.plus(
                maxInactiveInterval.getValue(),
                maxInactiveInterval.getUnit().toChronoUnit()
        );
    }

    @Override
    public TimeProperties getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public boolean isExpired() {

        return LocalDateTime
                .now()
                .plus(maxInactiveInterval.getValue(), maxInactiveInterval.getUnit().toChronoUnit())
                .isAfter(this.lastAccessedTime);
    }

    @Override
    public void setMaxInactiveInterval(TimeProperties maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    @Override
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    /**
     * 设置过期时间
     *
     * @param expirationTime 过期时间
     */
    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
