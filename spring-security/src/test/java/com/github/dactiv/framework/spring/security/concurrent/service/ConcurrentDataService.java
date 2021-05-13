package com.github.dactiv.framework.spring.security.concurrent.service;

import com.github.dactiv.framework.spring.security.concurrent.annotation.Concurrent;
import com.github.dactiv.framework.spring.security.entity.SocketUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConcurrentDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentDataService.class);

    public static int count = 0;

    @Concurrent("increment:count")
    public int increment() {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    @Concurrent(value = "increment:wait:count", waitTime = 3000)
    public int incrementWait() {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    @Concurrent(value = "increment:spring-el:count")
    public int incrementSpringEl() {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    @Concurrent(value = "increment:spring-el:count:[#socketUserDetails.id]")
    public int incrementArgs(SocketUserDetails socketUserDetails) {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
