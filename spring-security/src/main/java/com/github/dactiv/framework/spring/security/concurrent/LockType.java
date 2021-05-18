package com.github.dactiv.framework.spring.security.concurrent;

/**
 * 锁类型
 *
 * @author maurice.chen
 */
public enum LockType {

    /**
     * 安全锁
     */
    Lock,
    /**
     * 公平锁
     */
    FairLock
}
