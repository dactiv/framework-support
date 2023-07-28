package com.github.dactiv.framework.idempotent.test.concurrent;

import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

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

    @Concurrent(value = "increment:spring-el:count")
    public int incrementSpringEl() {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    @Concurrent(value = "increment:spring-el:count:[#entity.name]")
    public int incrementArgs(Entity entity) {
        count = count + 1;
        LOGGER.info("当前自增值为:" + count);
        return count;
    }

    @Concurrent(value = "increment:spring-el:count:[#entity.name]", condition = "[#entity.name] != null")
    public int incrementConditionArgs(Entity entity) {
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
